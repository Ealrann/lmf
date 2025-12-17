# LMF – Agent Notes

This repo is a multi‑module Gradle workspace for a textual meta‑modeling stack called **LMF** (Light Model Framework). It targets **Java 25**, uses the Java module system, and is built with Gradle 9.x.

If you plan to touch `.lm` meta‑models or generator behaviour, read `LMCORE_HOWTO.md` at the repo root first; it explains the LMCore language, how to structure M2 `.lm` files, and how generics map to the generated Java API.

## Modules Overview

- `logoce.lmf.core.api`  
  - Core **M3 meta‑model** definition (Meta‑Object‑Facility style).  
  - Defines the runtime model APIs and the LMCore generated Java API under `org.logoce.lmf.core.lang` (in `src/main/generated`).  
  - Provides generated Java API for the LMCore meta‑model under `org.logoce.lmf.core.lang` (in `src/main/generated`).  
  - Intended as the foundation to **load any `.lm` meta‑model (M2)** and, together with the generator, **produce Java code** for it.

- `logoce.lmf.core.loader`
  - Loader / parser / linker implementation and tooling APIs (`org.logoce.lmf.core.api.loader.*`).
  - Provides the external loading service implementation (`org.logoce.lmf.core.api.service.ILmLoader` provider).

- `logoce.lmf.extender`  
  - Generic adapter/extension framework (reflection helpers, adapter descriptors, registries, etc.).

- `logoce.lmf.adapter`  
  - Higher‑level adapter layer on top of `extender` providing adapter handles and basic adapter management.

- `logoce.lmf.notification`  
  - Lightweight notification/observer primitives (`INotifier`, `IFeature`, listener maps, etc.).

- `logoce.lmf.core.generator`  
  - JavaPoet‑based code generator.  
  - CLI entry point `org.logoce.lmf.generator.Main`:  
    - `arg[0]` = model `.lm` file, `arg[1]` = target directory.  
  - Uses the meta‑model from `logoce.lmf.core.api` to generate type‑safe Java APIs and builders for any `.lm` meta‑model.

- `logoce.lmf.gradle` and `logoce.lmf.gradle.test`  
  - Gradle plugin and test project to integrate `.lm` models and generated sources into a Gradle build.

## Coding Practices

When modifying or adding code:

- **Language level**
  - Assume **Java 25**; it is OK to use modern features (records, sealed interfaces/classes, pattern matching, `var`, streams, `Optional`, etc.) when they fit naturally.

- **Style**
  - Prefer `final var` for local variables where the type is obvious and immutability is intended.  
  - Use clear, descriptive names (no single‑letter identifiers except for short‑lived loop indices or obvious lambdas).  
  - Keep methods small and focused; push complex logic into well‑named helpers.
  - Favor small, well‑named methods over comments; prefer extracting behavior into small clearly named functions instead of explaining complex blocks with inline comments.
  - Minimize empty lines inside methods; keep related statements (for example consecutive `final var` declarations) together and only separate clearly distinct phases (setup, act, assert, etc.) with a single blank line.
  - Prefer the **Builder pattern** for constructing complex objects; when you introduce a builder for a type, place it as an inner builder class (static nested type) directly inside the built class.
  - Push decisions into small helper types or value objects (e.g. records) so that higher‑level code can stay declarative and stream/filter over those helpers.
  - Avoid parallel lists and index‑based coordination; prefer a single list of richer objects exposing the operations and predicates you need.
  - Construct domain objects with the minimum required state (e.g. immutable/mandatory parts) and then enrich them explicitly with optional or derived state.
  - Prefer local variables over mutable instance fields for temporary state needed only within a single method.
  - Keep lambdas and `forEach` blocks short and focused on a single side effect; move complex logic into named methods.
  - Prefer explicit two‑way branching (`if` / `else`) over multiple early returns when expressing core decision logic.
  - When both branches produce short expressions of the same shape, consider using the ternary operator for compactness and readability.

- **APIs and data structures**
  - Prefer immutable collections or unmodifiable views for externally visible state.  
  - Use Java streams where they improve clarity, but don’t over‑chain if a simple loop is clearer.  
  - Consider **records** for simple data carriers and **sealed types** for closed hierarchies (especially around model or token representations).

- **Model / generator specifics**
- `logoce.lmf.core.api` is the reference for the language semantics; align new behavior with existing tests under `src/test/java`.  
- Use `ModelRegistry.empty()` when you need a baseline registry that includes LMCore.  
- For programmatic loading in tools, tests, or an LSP, use `org.logoce.lmf.core.api.loader.LmLoader` from `logoce.lmf.core.loader` (`loadModel(...)`, `loadModels(...)`, `loadObjects(...)`).  
- Avoid calling legacy parsing/linking helpers directly; always go through `LmLoader` so that lex/interpret/link and diagnostics all stay consistent.

- **Modules and visibility**
  - Respect JPMS module boundaries; if you need access across modules, prefer adding targeted `exports`/`requires` rather than using raw/classpath tricks.  
- Avoid introducing new package cycles; keep generator‑specific logic in `logoce.lmf.core.generator` and runtime concerns in `logoce.lmf.core.api`.

## Tests and Validation

- There is good coverage in `logoce.lmf.core.api` (parsing, transformation, linking, and functional multi‑model behavior).  
- When changing core model or generator semantics, extend or adapt these tests rather than adding ad‑hoc ones elsewhere.  
- Prefer fast, focused tests over broad integration tests inside this repo.

## Quick Orientation for Newcomers

- **Where the language is defined**: LMCore’s own meta‑model lives in `logoce.lmf.core.api/src/main/model/asset/LMCore.lm`. This file shows the authoritative shapes for Groups, Features, Generics, Operations, etc. If something in `.lm` feels unclear, look here first.
- **Where generation logic lives**: `logoce.lmf.core.generator` consumes `.lm` models to emit Java. Generated sources for LMCore itself are under `logoce.lmf.core.api/src/main/generated`. Never hand‑edit generated files.
 - **How to load models in Java**:
   - External (Lily, etc.): `org.logoce.lmf.core.api.service.ILmLoader` (ServiceLoader-backed).
   - Internal tools/tests/LSP: `new org.logoce.lmf.core.api.loader.LmLoader(ModelRegistry.empty())` (from `logoce.lmf.core.loader`).
 - **Where LSP design notes live**: the `lsp-design/` folder at the root contains copied `.lm` examples, the LMCore how‑to, and several design docs explaining the language, editor challenges, and desired LSP features.
- **.lm syntax cheat sheet**:
  - `+att/-att` = Attribute (mutable/immutable). `+contains/-contains` = Relation with `contains=true`. `+refers/-refers` = Relation with `contains=false`.
  - `Group` is an abstract concept; `Definition` is a concrete group. `includes group=@Base` sets inheritance; pass generics via `(parameters ../../generics.0)` style hops.
  - Generics: declare with `(Generic T ...)` inside a group/definition. Reference them with relative paths (`../generics.N` or `/groups.X/generics.N`). For operations, provide `returnTypeParameters` and per‑parameter `parameters` blocks to carry type arguments.
  - Operations (pattern from LMCore): 
    ```lm
    (Operation name=collect content="return items;" returnType=@JavaList
        (returnTypeParameters type=/groups.0/generics.0)
        (OperationParameter name=items type=@JavaList
            (parameters type=/groups.0/generics.0)))
    ```
    Linker errors usually mean a missing/incorrect relative path or a containment that doesn’t exist (e.g., putting generics under a type that has no `generics` feature).
  - Cross‑model imports: add `imports=OtherModelDomain.Name` on `MetaModel` and reference external types with `#OtherModel@Type`.
- **Common pitfalls**:
  - JavaWrappers do not contain `generics` in LMCore; placing generics under them will fail to link.
  - Relative generic paths must be exact; `../../generics.0` counts from the current node up through parents.
  - Operations must carry their return type arguments in `returnTypeParameters`; parameter type arguments go in each `OperationParameter`’s `parameters`.
- **Debugging linker errors**: Messages like “Cannot find containment relation…” or “Cannot resolve named token …” indicate a missing feature on the parent group or a bad reference path. Compare against `LMCore.lm` to ensure your structure matches LMCore’s containment layout. Use existing tests (`logoce.lmf.core.api/src/test/java/...` and generator tests in `logoce.lmf.core.generator/src/test/java`) as working patterns.

## Operator responsibilities

- The operator (not the agent) manages git operations (status/commit/rebase/etc.). The agent should not run git commands unless explicitly told to do so.
