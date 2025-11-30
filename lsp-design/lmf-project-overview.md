# LMF Project Overview (for LSP Design)

This document gives a concise map of the LMF workspace and how `.lm` models are handled at runtime. It’s meant to orient an LSP/Lang tooling expert before diving into details.

## Modules and roles

The Gradle workspace contains several modules; the ones most relevant to LSP work are:

- `logoce.lmf.model`
  - Defines the **LMCore** meta‑model (M3) in `src/main/model/asset/LMCore.lm`.
  - Generated Java API for LMCore under `src/main/generated/org/logoce/lmf/model/lang`.
  - Runtime support to parse, interpret, and link `.lm` models into in‑memory objects:
    - Lexing: `org.logoce.lmf.model.lexer.*` + `LMIterableLexer`.
    - Parse trees: `org.logoce.lmf.model.resource.parsing.*`.
    - Interpretation (alias expansion + feature parsing): `org.logoce.lmf.model.resource.interpretation.*`.
    - Linking / reference resolution: `org.logoce.lmf.model.resource.linking.*`.
  - New, LSP‑friendly loader layer in `org.logoce.lmf.model.loader.*`:
    - `LmLoader` – high‑level API to load models from `InputStream` or `CharSequence`.
    - `LmTreeReader` – parses text into `Tree<PNode>` with diagnostics.
    - `LmModelLinker` – interprets and links parse trees into LMCore objects.

- `logoce.lmf.generator`
  - Consumes `MetaModel` instances (from `.lm` files) to generate Java APIs using JavaPoet.
  - CLI entry point: `org.logoce.lmf.generator.Main`.
    - Uses `ResourceUtil.loadObject/loadModels` (which now delegate to the new loader).
  - Test models under `src/test/model/`:
    - Show how users are expected to structure `.lm` M2 meta‑models.

- `logoce.lmf.editorfx`
  - A JavaFX editor that integrates the current lexer/interpreter/linker to provide a basic IDE‑like experience.
  - Uses the new loader stack (`LmLoader` / `LmTreeReader` / `LmModelLinker`) and builds its own symbol/semantic structures from `PNode` trees.
  - Serves as a reference for what is already possible, and what is currently missing (incrementality, advanced refactorings, etc.).

The other modules (`adapter`, `extender`, `notification`, `gradle`) provide infrastructure (adapters, extensibility, notifications, Gradle plugin) and are less central to LSP semantics, but will matter when we think about packaging and integration.

## Data flow: from `.lm` text to `MetaModel`

The new loader provides the cleanest view of the pipeline; it uses the existing lexing/linking code, but wraps it in LSP‑friendly APIs.

### 1. Lexing and parse tree

- Entry point:
  - `LmLoader` → `LmTreeReader` → `LMIterableLexer`.
- Key types:
  - `LMLexer` (generated from `src/main/grammar/lmf.flex`) produces `ELMTokenType` tokens:
    - `TYPE`, `TYPE_NAME`, `OPEN_NODE`, `CLOSE_NODE`, `VALUE`, `VALUE_NAME`, `ASSIGN`, `LIST_SEPARATOR`, `WHITE_SPACE`, `QUOTE`, `BAD_CHARACTER`.
  - `LMIterableLexer` wraps `LMLexer` and:
    - Tracks absolute offsets and lengths.
    - Handles **forced values** inside quotes (`"..."`), including escapes; these get converted into `VALUE` tokens and a closing `QUOTE`.
  - `PToken` = `{ value, type, offset, length }`.
  - `PNodeBuilder` + `PTreeBuilder` build simple S‑expression trees:
    - Every `( ... )` becomes a `Tree<PNode>`.
    - `PNode` is just a list of `PToken`s.

The grammar is intentionally shallow: it knows about S‑expression structure and key lexical categories, but not about LMCore semantics.

### 2. Interpretation: aliases and features

The second step is **interpretation**, done by `LMInterpreter`:

- Input: `Tree<PNode>`.
- Output: `Tree<PGroup<I>>`, where `PGroup` is:
  - The original `PNode`.
  - A `PType` (type + optional local name).
  - A list of `PFeature` (name + values, plus a boolean `isRelation`).

Important points:

- Aliases (`Alias` entries in `LMCore.lm`) are handled like macro expansions:
  - `LMInterpreter` looks up tokens in the alias map.
  - When it sees an alias, it re‑lexes the alias’s `value` string with `LMIterableLexer` to produce replacement tokens.
  - This is a **post‑lexing** step: alias expansion is not wired into the main lexer; it happens at interpretation time.
- `TokenParser` then interprets the flattened token sequence into:
  - A type token (e.g. `MetaModel`, `Group`, `Definition`, `+att`, `-contains`).
  - Named and unnamed value groups:
    - Named: `name=Foo` or `name Foo` → `PFeature(name="name", values=["Foo"])`.
    - Unnamed: a bare value, plus any contiguous list items.
  - Heuristics determine whether a feature is a “relation” (i.e. path/reference) or an attribute, based on the first value’s first character (`@`, `#`, `.`, `/` indicate a relation).

This interpretation layer is where much of the **editor complexity** starts:

- Aliases act like macros: one token in the source can expand into multiple semantic tokens.
- Many attributes are optional or unnamed; the interpreter has to reconstruct meaning from context.

### 3. Linking: from `PGroup` to LMCore objects

Actual semantic resolution is **meta‑model driven**:

- `LmModelLinker` is parameterized by a `ModelRegistry`.
  - It always includes LMCore (`LMCorePackage.Instance`) via `ModelRegistry.empty()`.
  - It uses `MetaModelRegistry.Instance.getAliasMap()` to interpret LMCore’s own aliases.
- `LinkNodeBuilder` maps the interpreted tree into `LinkNodeFull` trees:
  - Chooses the right LMCore `Group` for each `PGroup` (by name or by containment relation).
  - Determines the containing relation (from parent group to child group).
  - Attaches the raw `PFeature` list.
- `TreeToFeatureLinker` + `NodeLinker`:
  - For each LMCore `Group`, build resolvers for:
    - Attributes: enum/unit/java‑wrapper resolution.
    - Relations: `RelationResolver`, which uses:
      - `PathParser` for `@Local` and `#Model@Type` paths.
      - `LocalReferenceExplorer` for relative/local references (`./`, `../`, `@Name`).
      - `ModelReferenceResolver` for cross‑model references (`#OtherModel@Type`).
  - `ResolutionAttempt` captures either a `FeatureResolution` or a `NoSuchElementException` for each `PFeature`.
- `LinkNodeFull.build()`:
  - Guards against cyclic containment.
  - Injects child nodes via containment relations.
  - Applies all attribute and relation resolutions, or throws `LinkException` with a precise group + token message.
  - Finally calls the generated LMCore builder for the group.

The result is a tree of LMCore `LMObject`s (e.g. `MetaModel`, `Group`, `Enum`, `Relation`, etc.) representing the `.lm` file.

## New loader API (what LSP should use)

The new loader layer in `org.logoce.lmf.model.loader` is the intended integration point for an LSP server:

- `LmLoader`
  - `LmLoader(ModelRegistry)` – injects a model registry (initially `ModelRegistry.empty()` for LMCore).
  - `LmDocument loadModel(InputStream|CharSequence)`:
    - Returns `LmDocument(model, diagnostics, roots, source)`.
  - `List<Model> loadModels(List<InputStream>)`:
    - Multi‑model import‑aware build, preserving input order (compatible with the old `ResourceUtil.loadModels` behavior).
  - `List<? extends LMObject> loadObjects(...)`:
    - Generic root loading (for non‑model roots), mirrors legacy `ResourceUtil.loadObject`.
- `LmTreeReader`
  - Parses from `CharSequence`, not `InputStream`, which is ideal for in‑memory documents and incremental LSP updates.
- `LmModelLinker`
  - Two modes:
    - `linkModel(...)` – collects `LmDiagnostic`s, swallows `LinkException`s, and returns a partial result (tooling‑friendly).
    - `linkModelStrict(...)` – throws on link errors (legacy behavior used by `loadObjects`).

Current `ResourceUtil.loadObject/loadModel/loadModels` now delegate to `LmLoader`, so existing tools (generator, tests, editorfx) run through this pipeline as well.
