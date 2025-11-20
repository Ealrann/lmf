# LMF – Agent Notes

This repo is a multi‑module Gradle workspace for a textual meta‑modeling stack called **LMF** (Light Model Framework). It targets **Java 25**, uses the Java module system, and is built with Gradle 9.x.

If you plan to touch `.lm` meta‑models or generator behaviour, read `LMCORE_HOWTO.md` at the repo root first; it explains the LMCore language, how to structure M2 `.lm` files, and how generics map to the generated Java API.

## Modules Overview

- `logoce.lmf.model`  
  - Core **M3 meta‑model** definition (Meta‑Object‑Facility style).  
  - Can parse and interpret `.lm` files into in‑memory models via `ResourceUtil`, `PModelLinker`, and `ModelRegistry`.  
  - Provides generated Java API for the LMCore meta‑model under `org.logoce.lmf.model.lang` (in `src/main/generated`).  
  - Intended as the foundation to **load any `.lm` meta‑model (M2)** and, together with the generator, **produce Java code** for it.

- `logoce.lmf.extender`  
  - Generic adapter/extension framework (reflection helpers, adapter descriptors, registries, etc.).

- `logoce.lmf.adapter`  
  - Higher‑level adapter layer on top of `extender` providing adapter handles and basic adapter management.

- `logoce.lmf.notification`  
  - Lightweight notification/observer primitives (`INotifier`, `IFeature`, listener maps, etc.).

- `logoce.lmf.generator`  
  - JavaPoet‑based code generator.  
  - CLI entry point `org.logoce.lmf.generator.Main`:  
    - `arg[0]` = model `.lm` file, `arg[1]` = target directory.  
  - Uses the meta‑model from `logoce.lmf.model` to generate type‑safe Java APIs and builders for any `.lm` meta‑model.

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
  - `logoce.lmf.model` is the reference for the language semantics; align new behavior with existing tests under `src/test/java`.  
  - Use `ModelRegistry.empty()` when you need a baseline registry that includes LMCore.  
  - For multi‑model scenarios, prefer the existing `ResourceUtil.loadModel/loadModels` and `PModelLinker` instead of rolling custom loaders.

- **Modules and visibility**
  - Respect JPMS module boundaries; if you need access across modules, prefer adding targeted `exports`/`requires` rather than using raw/classpath tricks.  
  - Avoid introducing new package cycles; keep generator‑specific logic in `logoce.lmf.generator` and runtime concerns in `logoce.lmf.model`.

## Tests and Validation

- There is good coverage in `logoce.lmf.model` (parsing, transformation, linking, and functional multi‑model behavior).  
- When changing core model or generator semantics, extend or adapt these tests rather than adding ad‑hoc ones elsewhere.  
- Prefer fast, focused tests over broad integration tests inside this repo.

## Operator responsibilities

- The operator (not the agent) manages git operations (status/commit/rebase/etc.). The agent should not run git commands unless explicitly told to do so.
