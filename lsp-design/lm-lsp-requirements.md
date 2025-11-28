# LSP Requirements for `.lm`

This document outlines the desired behavior of a future LM LSP server, building on top of the new loader (`org.logoce.lmf.model.loader`) and the existing runtime.

The goal is to provide **first‑class language tooling** for `.lm`, not just syntax highlighting.

## 1. Core features

At minimum, we want:

- **Syntactic diagnostics**
  - Real‑time error reporting on:
    - Lexing problems (e.g. bad characters, unterminated quoted values).
    - Unbalanced parentheses / malformed S‑expressions.
  - Diagnostics should map to precise spans, using `PToken.offset/length` as in `LmTreeReader`.

- **Semantic diagnostics**
  - All link‑time errors surfaced via `LmModelLinker`:
    - Unresolved model imports or unknown model names.
    - Unresolved group/definition names, enum literals, units, wrappers.
    - Invalid generics paths or bounds.
    - Cyclic containment (as detected by `LinkNodeFull.build()`).
  - Clear messages that mention both the token and the semantic context (group, feature).
  - Consistent mapping to LSP `Diagnostic` severity and possible error codes.

- **Symbols and outline**
  - Document symbols:
    - `MetaModel`, `Group`, `Definition`, `Enum`, `Unit`, `Generic`, operations, etc.
  - Hierarchical outline:
    - Model → groups/definitions/enums/units → features → operations → parameters.
  - Using `PNode` / `PFeature` spans and `LmDocument.roots` for positions.

- **Go‑to definition and references**
  - For:
    - Groups/definitions (`Group`, `Definition`).
    - Enums and enum literals.
    - Units and `JavaWrapper`s.
    - Relation targets (`@Type`, `#Model@Type`).
    - Generics and their uses.
  - Must tolerate incomplete or partially broken models (best‑effort behavior).

- **Rename symbol**
  - Across:
    - A single model file.
    - Multiple files (via imports), where possible.
  - Covers:
    - Group/definition names.
    - Enum names and literals.
    - Feature names (`+att`, `+contains` features).
    - Generic parameter names (e.g. `T`, `UnaryType`, `EffectiveType`).
  - Needs to:
    - Update references encoded as paths (`#Model@Type`, `@Name`, relative generics).
    - Update alias uses where relevant (e.g. renaming a group used in an alias’s `value`).

## 2. Advanced features

We would like to aim for:

- **Auto‑formatting**
  - Consistent indentation and line breaks for S‑expressions.
  - Configurable style for:
    - Single‑line vs multi‑line nodes.
    - Attribute ordering.
    - Whether to expand or preserve aliases (e.g. always print `[1..*]` vs expand to `mandatory many`).
  - Idempotent formatter: re‑formatting an already formatted document should produce minimal changes.

- **Code completion**
  - Context‑aware suggestions:
    - After `(`, suggest valid type keywords (`MetaModel`, `Group`, `Definition`, `Enum`, `Unit`, etc.).
    - Inside a `MetaModel`, suggest valid children.
    - Inside `Group` / `Definition`, suggest `+att`, `-att`, `+contains`, `+refers`, etc.
    - Within `datatype=`, `@...`, `#...`, suggest appropriate types (enums, units, groups, external models).
    - For generics, suggest accessible generic parameters via relative paths.
  - Use the current (possibly partially linked) model and `ModelRegistry` to inform suggestions.

- **Refactorings and code actions**
  - “Introduce alias” for repeated attribute patterns.
  - “Expand alias” into explicit attributes.
  - “Convert implicit name to explicit `name=...`”.
  - “Normalize multiplicity” (e.g. expand `[1..*]` to attributes, or vice versa).
  - “Extract sub‑model” or “extract `Group`/`Definition`” (longer‑term).

- **Cross‑model operations**
  - Project‑wide:
    - Find all implementations of a `Group` (i.e. all `Definition`s that include it).
    - Find all imports of a given `MetaModel`.
  - Multi‑file rename of a `MetaModel` or `Group`.

- **Hover and quick documentation**
  - On LMCore features, show:
    - The underlying Java type (`Group`, `Relation<T,E>`, etc.).
    - The meaning of flags (`contains`, `immutable`, `mandatory`, `many`).
  - On user‑defined features, show:
    - Data type, multiplicity, containment, default values.
  - Possibly integrate the LMCORE_HOWTO information for LMCore primitives and patterns.

## 3. Architectural expectations

Given the existing runtime, an LSP server will need to:

- **Use `LmLoader` as the central entry point**
  - For each open document:
    - Keep the raw `CharSequence`.
    - Call `LmTreeReader` for lex/parse (or reuse `LmLoader.loadModel`).
    - Use `LmModelLinker` to build a semantic model and collect diagnostics.
  - For project‑wide modeling:
    - Use `LmLoader.loadModels` with a `ModelRegistry` containing all known models.

- **Maintain incremental state**
  - Cache:
    - Parsed trees (`Tree<PNode>`).
    - Linked structures (`LinkNodeFull` / `LMObject` graphs).
    - A project‑wide `ModelRegistry`.
  - On text change:
    - For minimal implementation: re‑parse the changed file and re‑link all models that depend on it.
    - Longer‑term: consider incremental parse/link (e.g. reusing parts of the tree where possible).

- **Integrate with existing linking classes, not reimplement them**
  - Use:
    - `LMInterpreter` for alias expansion and PGroup/PFeature construction.
    - `TreeToFeatureLinker` and `NodeLinker` for attribute/relation resolution.
    - `LocalReferenceExplorer`, `ModelReferenceResolver`, and `ImportResolver` for path and import semantics.
  - Extend **around** these classes to capture:
    - Cross‑file symbol indices.
    - Maps from textual occurrences to semantic objects.

## 4. Open design questions for the expert

These are the topics where we’d especially like LSP expertise:

- **Incrementality**
  - How to best integrate an S‑expression grammar with incremental parsing (possibly with a library, or with a custom incremental layer around `LMIterableLexer` and `PNodeBuilder`)?
  - Strategies to reuse `LMInterpreter` and `LmModelLinker` incrementally (per subtree) rather than always from the root.

- **AST and CST representation**
  - Whether to introduce an explicit CST/AST for `.lm`:
    - Right now we have:
      - A low‑level CST (`Tree<PNode>`).
      - A semantic tree (`LinkNodeFull` + `LMObject`s).
    - Would an intermediate typed AST (e.g. `MetaModelNode`, `GroupNode`, `FeatureNode`) help anchor LSP features and formatting?

- **Formatting and printing**
  - How to design a printer that:
    - Respects user formatting as much as possible.
    - Can still normalize when requested.
  - How to represent comments and whitespace (“trivia”) so they can be preserved through edits.

- **Project model**
  - Best practices for managing a **graph of models** with imports:
    - When to re‑build the `ModelRegistry`.
    - How to persist or recompute the multi‑model graph efficiently on changes.

- **Refactoring safety**
  - How to structure rename, extract, and other refactors so they are:
    - Safe across imports (e.g. renaming a `Group` used by other models).
    - Atomic (all or nothing).
    - Understandable by users (clear preview and diff).

We’d like to iterate on these designs together, using this folder (`lsp-design/`) as a shared, versioned knowledge base for the LM LSP effort.

