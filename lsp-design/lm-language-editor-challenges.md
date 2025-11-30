# `.lm` Language – Editor and LSP Challenges

This document explains why building a robust editor/LSP for `.lm` is non‑trivial, beyond “just parse an S‑expression”. It focuses on areas where we will need careful design and likely extra infrastructure for an LSP server.

The examples referenced below live under:

- `lsp-design/models/LMCore.lm.txt`
- `lsp-design/models/CarCompany.lm.txt`
- `lsp-design/models/Graph*.lm.txt`
- `lsp-design/models/NativeGenerics.lm.txt`
- `lsp-design/models/OperationsGeneric.lm.txt`

## 1. Surface syntax vs semantics

The surface syntax is deceptively simple:

- S‑expression style:
  ```lm
  (MetaModel domain=test.model name=CarCompany
      (Group Entity
          (+att name=name datatype=#LMCore@string [1..1]))
      (Enum Brand Renault,Peugeot)
      (Definition Car
          (includes group=@Entity)
          (-att name=brand datatype=@Brand [1..1] defaultValue="Peugeot")
          (+contains name=passengers @Person [0..*]))
      ...)
  ```
- Almost every construct is a `(...)` node with:
  - A type (`MetaModel`, `Group`, `Definition`, `Enum`, `Unit`, `Generic`, operations, etc.).
  - A series of attributes and relations encoded as name/value pairs or bare values.

However, **the parsing of tokens into meaningful fields is not context‑free**:

- The same token can be:
  - A type name.
  - A feature name (`name=`, `datatype=`, `contains`, `lazy`, etc.).
  - A value, with or without an explicit `name=`.
- Many features can be omitted or reordered; alias expansion can change the apparent structure.

The core interpreter (`TokenParser`, `LMInterpreter`) uses LMCore semantics implicitly to decode this, and an editor has to mirror that understanding to provide meaningful features.

## 2. Alias system (macro‑like)

Aliases in LMCore act as small macros over the meta‑model:

- See the bottom of `LMCore.lm`:
  ```lm
  (Alias +contains value="Relation contains immutable=false")
  (Alias -contains value="Relation contains immutable")
  (Alias +refers value="Relation contains=false immutable=false")
  ...
  (Alias [0..1] value="mandatory=false many=false")
  (Alias [1..*] value="mandatory many")
  ```
- This allows concise syntax:
  ```lm
  (+contains name=passengers @Person [0..*])
  ```
  which expands into a `Relation` node with the right flags set.

Implementation details:

- `LMInterpreter` has access to an alias map (from `MetaModelRegistry`, populated from LMCore).
- For each `PToken`, it checks if the token value is an alias key; if so, it:
  - Takes the alias’s `value` string (e.g. `"Relation contains immutable=false"`).
  - Re‑lexes that string using `LMIterableLexer`.
  - Splices the resulting `PToken`s into the token stream.

**Implications for an editor/LSP:**

- Aliases are expanded post‑lexing; they can produce multiple semantic tokens from one textual token.
  - A naive token stream in the editor won’t match the semantic structure; we need to track both:
    - Original tokens / spans (for highlighting, renaming, etc.).
    - Expanded tokens / semantic view (for linking, completion, refactoring).
- Aliases can hide structure:
  - The user types `+contains`, but semantically it’s a `Relation` with `contains=true`, `immutable=false`.
  - LSP features like “go to definition of feature `contains`” must know that `+contains` means a particular `Relation` feature.
- For refactorings, updating alias definitions can globally impact meaning:
  - Changing `Alias [1..*]` affects how multiplicities are interpreted across all models that use it.

We will likely want an explicit **alias expansion map** in the LSP: for each alias use, a link back to:

- The alias definition (`Alias` node in LMCore or in the current model).
- The expanded syntax tree segment it produced.

## 3. Loose attribute syntax and heuristic parsing

Many attributes in `.lm` have flexible syntax:

- You can write:
  ```lm
  (Enum Brand Renault,Peugeot)
  ```
  instead of:
  ```lm
  (Enum name=Brand literals=Renault,Peugeot)
  ```
- In `CarCompany.lm`:
  - `(+contains cars [0..*] @Car)` uses positional values: feature name, multiplicity alias, target type.
  - The same semantics could be written with explicit attributes (`name=cars`, `mandatory=false`, `many=true`, `concept=@Car`).

The interpreter handles this via `TokenParser`:

- Distinguishes `VALUE_NAME` vs `VALUE` tokens (based on regexes in `lmf.flex`).
- Applies heuristics:
  - If the first token after the type is not `name=...` and does not contain `=`, treat it as the implicit name.
  - For multiplicities (`[0..1]`, `[1..*]`), uses aliases in LMCore to expand into `mandatory/many` attributes.
  - For relations, looks at the first value’s leading character / alias (`@`, `#`, `.`, `/`) to classify as relation vs attribute.

**Challenges for an editor:**

- The same semantic field can show up in multiple syntactic forms:
  - `name=Foo`, `name Foo`, bare `Foo` as first value.
  - `[0..*]` or explicit `mandatory=false many=true`.
- When we want to implement:
  - “Rename feature `name` → `identifier` across the model”,
  - “Offer quick fixes (e.g. convert shorthand multiplicity into explicit attributes)”,
  the LSP must understand all these patterns and map them to the underlying LMCore features.
- Code actions like “add missing `name=`” or “normalize attribute syntax” will need to reconstruct and re‑print the text in a consistent style.

## 4. Path‑based references and relative navigation

References in `.lm` are text paths with their own mini‑language, parsed by `PathParser`:

- Local references:
  - `@TypeName` – by simple name in the current model.
  - `./feature` / `../feature` – relative navigation in the link tree (used especially for generics).
  - `children.0`, `children.1` – index‑based navigation inside collections.
- Cross‑model references:
  - `#OtherModel@TypeName` – refer to an imported model’s type.
    - The `ImportResolver` resolves `OtherModel` to a qualified name via:
      - `LMCore` special handling.
      - The owning model’s `imports` list.

Examples:

- Generics in `NativeGenerics.lm` and `OperationsGeneric.lm` use relative paths like:
  ```lm
  (parameters type=/groups.0/generics.0)
  (parameters wildcard=true wildcardBoundType=Super type=../../../../generics.0)
  ```
- Multi‑model cases in `Graph*.lm` use `#GraphCore@Node`, `#GraphExtensions@Graph`, etc.

Resolution logic is scattered across:

- `PathParser` – splits the string into steps (`ROOT`, `PARENT`, `CHILD`, `NAME`, `MODEL`).
- `LocalReferenceExplorer` – walks the **link node tree**, not the syntax tree, to resolve relative paths and `@Name` references.
- `ModelReferenceResolver` – walks **built model objects** to resolve cross‑model references and names.
- `ImportResolver` – maps model short names to fully qualified names and `Model` instances via `ModelRegistry`.

**Implications for LSP:**

- Go‑to‑definition, find references, and rename across files must incorporate:
  - The link tree and built model objects (semantics).
  - The original textual paths (syntax).
- Partial or incorrect paths (e.g. user typing `../gen`) need to be parsed and understood:
  - Completions should show valid steps from the current context.
  - Diagnostics must be precise enough to underline the problematic segment.
- For multi‑file operations (e.g. rename a `Group` across a project):
  - We need project‑wide `ModelRegistry` state and an index of where each `Group`/`Definition` is defined and referenced.

## 5. Generics and relative semantics

Generics make the language more expressive but add another layer of indirection:

- LMCore’s `Feature`, `Attribute`, `Relation` are generic types.
- `.lm` expresses generics via:
  - `(Generic T)` blocks under groups / definitions.
  - `(parameters ...)` blocks on `includes`, relations, and operation types.
  - Relative paths like `../generics.0` or `/groups.0/generics.0` to refer to generic parameters.

For example, in `NativeGenerics.lm`:

```lm
(Definition NativeContainer
    (Generic T
        (extension boundType=Extends type=#LMCore@float))
    (+att name=value datatype=@NativeBox [0..1]
        (parameters type=/groups.0/generics.0)))
```

The linker must ensure that:

- The `parameters` path points to the right `Generic` declaration.
- Bounds (`Extends`, `Super`) are compatible.

For editor/LSP features:

- Hover / signature help should show the resolved generic type arguments.
- Rename of a `Generic T` should update both the declaration and all relative references.
- Diagnostics for generics are often non‑local: renaming a generic or changing bounds can break type arguments at distant call sites.

## 6. Multi‑model import graph

`.lm` models can import each other:

- In `FrenchCarCompany.lm`:
  ```lm
  (MetaModel domain=test.model2 name=FrenchCarCompany imports=test.model.CarCompany
      (Definition French
          (includes #CarCompany@Entity)
          (+refers favoriteCar [0..1] #CarCompany@Car))
  )
  ```
- The loader must:
  - Load all models in an order that respects imports.
  - Resolve `#CarCompany` via the importing model’s `imports` list and the `ModelRegistry`.

For an LSP:

- A project can contain many `.lm` files with arbitrary import graphs (including cycles or missing imports).
- We need:
  - A robust multi‑document loader (which we now have in `LmLoader.loadModels`).
  - Incremental updates: when one document changes, recompute only affected models and their dependants.
  - Diagnostics that distinguish:
    - “Your code is syntactically invalid.”
    - “Some imported model could not be loaded or has errors.”

## 7. Error handling and partial models

The existing tooling primarily uses a “fail fast” approach in some places and “diagnostic + partial model” in others:

- The loader:
  - `LmLoader.loadModel(...)` returns an `LmDocument` with:
    - `model()` (possibly `null`).
    - `diagnostics()` (list of `LmDiagnostic`s).
    - `roots()` (parse trees).
    - `source()` text.
  - Internally, `LmModelLinker.linkModel(...)` swallows `LinkException`s and reports them as `LmDiagnostic`s.
  - `LmModelLinker.linkModelStrict(...)` still propagates exceptions (used by `loadObjects`).

For an LSP:

- We always want to produce **some** parse tree and as much semantic information as possible, even in the presence of errors (for completions, symbol outline, etc.).
- The current patterns are a solid base, but we will likely need:
  - Better separation between parse/lex errors and semantic/link errors.
  - Structured error codes for LSP diagnostics (e.g. `UNRESOLVED_MODEL`, `UNRESOLVED_PATH`, `INVALID_ALIAS`, `CYCLIC_CONTAINMENT`).
  - Strategies to keep partial link trees and objects alive when some references fail.

## 8. Printing and formatting

There is no dedicated, structured pretty‑printer today:

- Generating `.lm` text is mostly done via ad‑hoc code when needed.
- For LSP features (formatting, code actions that rewrite code), we need:
  - A canonical formatting strategy that respects:
    - S‑expression alignment and indentation.
    - Where aliases are used vs expanded.
    - How optional/implicit attributes are printed.
  - A way to round‑trip:
    - Parse → semantic model → modified semantic model → text edits,
    - Without losing comments or reordering too aggressively.

Comments (e.g. `;;` style) and other non‑semantic trivia are not fully represented in the current trees; we may need to extend the parsing layer or keep a “trivia” channel in tokens if advanced formatting is required.
