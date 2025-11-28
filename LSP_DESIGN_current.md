This document describes the **current design and implementation state** of the LMF LSP project, and what remains to be done. It merges and supersedes the earlier design docs:

- `LSP_DESIGN.md`
- `LSP_DESIGN_analysis.md`
- `LSP_DESIGN_precision.md`

Those original files are kept for historical context; this file is the up‑to‑date view.

---

## 0. Scope and goals

The LMF LSP project has three main goals:

1. Provide a **first‑class authoring experience** for `.lm` meta‑models (M2) and, later, LM instance models (M1).
2. Offer robust, cross‑file and cross‑level refactoring:
   - Preserve references across imports.
   - Propagate changes from meta‑models to their instance models where possible.
3. Be **tool‑friendly**:
   - Good incremental behavior (no full rebuild on every keystroke).
   - Clear data structures and extension points so other tools (IntelliJ plugin, CI, AI agents) can query and modify models at the semantic level.

The design is deliberately split into:

- The **LM LSP server** (`logoce.lmf.lsp`): language semantics, parsing/linking, indices, core LSP functions.
- The **IntelliJ plugin** (`logoce.lmf.intellij`): wiring `.lm` files in IntelliJ IDEA 2025.x to the LM LSP server via the IntelliJ LSP API.

---

## 1. Server architecture (logoce.lmf.lsp)

### 1.1. Top‑level architecture

The LSP server is a single process exposing:

- Standard LSP endpoints (`initialize`, `shutdown`, `exit`, `textDocument/*`, `workspace/*`).
- Language services built on top of the official LM runtime pipeline:
  - `LMLexer` → `LMIterableLexer` → `Tree<PNode>` → `PGroup`/`PFeature` → `LinkNodeFull` → `LMObject` graph.

Implementation entry point:

- `org.logoce.lmf.lsp.Main`
  - Uses LSP4J `LSPLauncher.createServerLauncher(server, in, out)` to speak LSP over stdio.
- `org.logoce.lmf.lsp.LmLanguageServer`
  - Implements `LanguageServer`, `LanguageClientAware`.
  - Owns:
    - Single worker `ExecutorService` (`lm-lsp-worker`).
    - `WorkspaceIndex` (in‑memory model graph + indices).
    - `LmTextDocumentService` and `LmWorkspaceService`.

All heavy work (parse/link/indexing) is executed on the single worker executor; LSP4J threads submit tasks to this worker to keep state consistent.

### 1.2. Core data model

The server wraps the LM runtime pipeline in LSP‑friendly snapshots and indices.

#### 1.2.1. SyntaxSnapshot

`org.logoce.lmf.lsp.state.SyntaxSnapshot`:

- `List<PToken> tokens` – currently unused placeholder; future incremental work may populate this.
- `List<Tree<PNode>> roots` – S‑expression CST trees from `LmTreeReader`.
- `List<LmDiagnostic> diagnostics` – lexical/parse diagnostics (bad characters, unterminated quotes, unbalanced parentheses).
- `CharSequence source` – full document text.

Built via:

- `LmTreeReader.read(CharSequence, List<LmDiagnostic>)` (using `LMIterableLexer` + `PNodeBuilder`).

#### 1.2.2. SemanticSnapshot

`org.logoce.lmf.lsp.state.SemanticSnapshot`:

- `Model model` – LMCore root model (`MetaModel` or other).
- `List<? extends LinkNode<?, PNode>> linkTrees` – link nodes (`LinkNodeFull`) produced by `LmModelLinker`.
- `List<LmDiagnostic> diagnostics` – link diagnostics (unresolved types, imports, generics, cyclic containment).
- `SymbolTable symbolTable` – per‑document symbol entries (currently unused; global indices are maintained in `WorkspaceIndex`).
- `List<ReferenceOccurrence> references` – per‑document references (global indices also live in `WorkspaceIndex`).

Built via:

- `LmModelLinker.linkModel(roots, diagnostics, source)` using the current `ModelRegistry`.

#### 1.2.3. WorkspaceIndex & global indices

`org.logoce.lmf.lsp.state.WorkspaceIndex` owns:

- `documents: Map<URI,LmDocumentState>` – all open documents.
- `modelRegistry: ModelRegistry` – shared registry of `Model`s, always including LMCore.
- Global symbol and reference indices:
  - `symbolIndex: Map<SymbolId, SymbolEntry>`
  - `symbolsByUri: Map<URI, List<SymbolId>>`
  - `referenceIndex: Map<SymbolId, List<ReferenceOccurrence>>`
  - `referencesByUri: Map<URI, List<ReferenceOccurrence>>`

Where:

- `ModelKey(domain, name)` – identifies a model.
- `LmSymbolKind` – `META_MODEL`, `TYPE`, `FEATURE`.
- `SymbolId(ModelKey, LmSymbolKind, name)` – logical symbol identifier.
- `SymbolEntry(SymbolId, URI, Range)` – declaration location in a document.
- `ReferenceOccurrence(SymbolId target, URI uri, Range range)` – a single reference occurrence.

`WorkspaceIndex` provides helper methods to:

- Rebuild/remove indices per document (`registerSymbols`, `registerReferences`, `clearIndicesForDocument`).
- Read per‑document symbols/references (`symbolsForUri`, `referencesForUri`).

---

## 2. Server pipelines and features

### 2.1. Document analysis pipeline

`LmLanguageServer.analyzeDocument(LmDocumentState)`:

1. **Syntax**:
   - Invokes `LmTreeReader.read(text, syntaxDiagnostics)` → `roots`, `source`.
   - Builds and stores `SyntaxSnapshot`.
2. **Semantics**:
   - Invokes `LmModelLinker<PNode>(modelRegistry).linkModel(roots, semanticDiagnostics, source)` → `model`, `linkTrees`.
   - Builds and stores `SemanticSnapshot`.
3. **Indices**:
   - Calls `rebuildIndicesForDocument(state)` (see below).
4. **Diagnostics**:
   - Calls `publishDiagnostics(state)`, merging syntax + semantic diagnostics into LSP `Diagnostic`s using `TextPositions` for line/column.

`rebuildWorkspace()`:

- Rebuilds `ModelRegistry` from **all open documents**:
  - Serializes each document’s text to an `InputStream`.
  - Calls `LmLoader.withEmptyRegistry().loadModels(inputs)`, which preserves import semantics and ordering.
  - Constructs a new `ModelRegistry.Builder` from `ModelRegistry.empty()`, registers all models, and stores `builder.build()` in `WorkspaceIndex`.
- Re‑analyzes all documents with the new registry.

### 2.2. Symbol and reference indexing

`rebuildIndicesForDocument(LmDocumentState state)`:

- If the document’s semantic model is a `MetaModel mm`:
  - Creates `ModelKey key = new ModelKey(mm.domain(), mm.name())`.
  - Uses `DocumentSymbols.buildDocumentSymbols(syntax)` to produce LSP `DocumentSymbol`s from the CST.
  - Flattens these to `SymbolEntry`s (MetaModel, groups, definitions, enums, units, aliases, JavaWrappers, generics, attributes/relations/operations) and calls:
    - `workspaceIndex.registerSymbols(uri, symbolEntries)`.
  - Builds type references by scanning tokens:
    - `@Type` → a local `TYPE` symbol in the same `MetaModel`.
    - `#Model@Type` → uses `ModelRegistry` to resolve `Model` by name and target `TYPE` in another `MetaModel`.
    - Adds a `ReferenceOccurrence` for each match, using token ranges computed from `PToken.offset`/`length` via `TextPositions`.
    - Calls `workspaceIndex.registerReferences(uri, references)`.
- If the model is not a `MetaModel`, it clears indices for that document for now.

Symbol kinds:

- `META_MODEL` – root `MetaModel`.
- `TYPE` – `Group`, `Definition`, `Enum`, `Unit`, `JavaWrapper`.
- `FEATURE` – `Attribute`, `Relation`, `Operation`, generics, etc.

### 2.3. LSP feature implementations

#### 2.3.1. Capabilities (initialize)

Currently in code, `LmLanguageServer.initialize(...)` only sets:

```java
capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
```

This must be extended so IntelliJ knows which features are supported. The intended set is:

```java
capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
capabilities.setCompletionProvider(new CompletionOptions());
capabilities.setDefinitionProvider(true);
capabilities.setReferencesProvider(true);
capabilities.setHoverProvider(true);
capabilities.setDocumentSymbolProvider(true);
capabilities.setRenameProvider(true);
// further flags as we implement more (codeAction, formatting, etc.)
```

Once this is done, IntelliJ’s LSP client will systematically send `definition`, `references`, `hover`, `rename`, and `documentSymbol` requests, which are already implemented.

#### 2.3.2. Implemented textDocument features

In `LmTextDocumentService`:

- `didOpen`, `didChange`, `didClose`, `didSave` → update `WorkspaceIndex`, call `rebuildWorkspace()` (which in turn analyzes documents).
- `documentSymbol(DocumentSymbolParams)`:
  - Uses `DocumentSymbols.buildDocumentSymbols(SyntaxSnapshot)` to build a hierarchical symbol outline.
- `completion(CompletionParams)`:
  - Currently returns a simple keyword list (`MetaModel`, `Group`, `Definition`, `Enum`, `Unit`, `Alias`, `JavaWrapper`, `Generic`, `Operation`, plus feature aliases `+att`, `+contains`, etc.), independent of context.
- `definition(DefinitionParams)`:
  - Uses `LmLanguageServer.findTargetSymbol(uri, position)` to resolve a `SymbolId`.
  - Looks up `SymbolEntry` in `WorkspaceIndex.symbolIndex`.
  - Returns the definition location as `Location`.
- `references(ReferenceParams)`:
  - Uses `findTargetSymbol` to find the symbol at cursor.
  - Returns:
    - Declaration location (if `includeDeclaration` is true).
    - All `ReferenceOccurrence`s for that `SymbolId` from `WorkspaceIndex.referenceIndex`.
- `hover(HoverParams)`:
  - Uses `findTargetSymbol` to get `SymbolId`.
  - Returns a simple `Hover` text: `"MetaModel/Type/Feature name in <domain.name>"`.
- `rename(RenameParams)`:
  - Checks `Settings.experimentalRename`.
  - Uses `findTargetSymbol` to get `SymbolId`.
  - For `TYPE`/`FEATURE` symbols (but not `META_MODEL`):
    - Adds a `TextEdit` for the declaration range.
    - Adds a `TextEdit` for each reference occurrence’s range.
  - Returns a `WorkspaceEdit` mapping URIs to edits.

#### 2.3.3. Diagnostics

Diagnostics are merged and published via:

- `LmLanguageServer.publishDiagnostics(LmDocumentState state)`:
  - Combines syntax and semantic `LmDiagnostic`s into LSP `Diagnostic`s with proper ranges and severity.
  - Uses `LanguageClient.publishDiagnostics`.

---

## 3. IntelliJ plugin wiring (logoce.lmf.intellij)

The IntelliJ plugin:

- Declares `.lm` as a file type (`LMF`).
- Depends on the LSP module.
- Registers `LmLspServerSupportProvider` which starts the LM LSP server when `.lm` files are opened.

See `logoce.lmf.intellij/README.md` for full detail; the key points are summarized here.

### 3.1. How IntelliJ starts the LM LSP server

- When a `.lm` file is opened:
  - IntelliJ calls `LmLspServerSupportProvider.fileOpened(...)`.
  - If the extension is `lm`, it calls `ensureServerStarted(new LmLspServerDescriptor(project))`.
- `LmLspServerDescriptor`:
  - Extends `ProjectWideLspServerDescriptor`.
  - `isSupportedFile` returns true for `.lm`.
  - `createCommandLine` returns:

    ```java
    new GeneralCommandLine(List.of("logoce.lmf.lsp"));
    ```

  - That process is expected to be `logoce.lmf.lsp` from `:logoce.lmf.lsp:installDist`, communicating over stdio.

Once the server is up and capabilities are correctly advertised, IntelliJ routes all LSP‑supported editor features (diagnostics, outline, goto, references, rename, completion, etc.) through it.

---

## 4. Remaining work and priorities

### 4.1. Capabilities handshake

**High priority**: extend `LmLanguageServer.initialize(...)` to set `ServerCapabilities` flags for all implemented features (`completion`, `definition`, `references`, `documentSymbol`, `hover`, `rename` at least). Without this, IntelliJ won’t reliably send these requests even though handlers exist.

### 4.2. Completion

- Current: static list of keywords.
- Desired:
  - Context‑aware completion for:
    - Node heads (`MetaModel`, `Group`, `Definition`, etc.).
    - Feature aliases inside groups/definitions (`+att`, `+contains`, etc.).
    - `datatype=` and `@`/`#` positions → suggest types/enums/units from `ModelRegistry`.
    - Generics path completions (`../generics.0`, `/groups.0/generics.0`) using `PathUtil` and link context.

### 4.3. Hover richness

- Current: `"MetaModel/Type/Feature name in model"`.
- Desired:
  - For LMCore types & features:
    - Underlying LMCore type (`Group`, `Relation<UnaryType,EffectiveType>`, etc.).
    - Datatype, multiplicity, containment, default values.
  - For user‑defined types and features:
    - Same information inferred from the linked `LMObject` graph.

### 4.4. Code actions and formatting

- Currently unimplemented.
- Desired:
  - Quick fixes:
    - “Add missing import for model X”.
    - “Expand alias” / “Use alias form”.
    - “Make name explicit” for implicit feature names.
  - Formatter:
    - Consistent S‑expression layout (indentation, line breaks).
    - Options to preserve or normalize alias usage (`[1..*]` vs `mandatory many`).

### 4.5. Instance models (M1) and cross‑level refactoring

- Current design focuses on M2 `MetaModel` documents; M1 “instance model” syntax and conformance are intentionally left flexible.
- Remaining design/implementation:
  - `ConformanceAnalyzer` interface to map instance models back to M2 features.
  - Populating `ReferenceOccurrence` entries for structural usages of features in instance models.
  - Extending rename / refactors to propagate from M2 to M1 where safe (e.g. renaming `Car.color` attribute updates property names in instance models).

### 4.6. IntelliJ plugin refinements

- Make LM LSP launcher path configurable (plugin settings or system property) instead of hardcoding `logoce.lmf.lsp`.
- Optionally:
  - Provide a simple syntax highlighter for `.lm` on top of semantic highlighting.
  - Expose LM‑specific settings (e.g. formatter options, experimental features) in IntelliJ’s Settings UI and map them to LSP `workspace/didChangeConfiguration`.

