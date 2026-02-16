# LMF (Light Model Framework)

LMF is a lightweight, modern rethinking of EMF for Java projects.
Like EMF, LMF is a Model-Driven Engineering (MDE) framework.
It follows the same core model-driven idea as EMF, but with a stronger focus on a textual workflow (`.lm` files), JPMS-friendly modularity, and pragmatic tooling for generation, validation, and editing.

LMF is not presented as a strict drop-in replacement for EMF.
It is better described as a lean, model-centric framework inspired by EMF principles, designed for teams that want a simpler and more explicit developer experience around model authoring and runtime integration.

## Why LMF exists

LMF aims to make model-driven development easier to adopt in modern Java codebases by:

- keeping models human-readable and version-control friendly through a textual language,
- generating typed Java APIs from meta-models,
- supporting runtime loading and linking of model graphs,
- enabling extension/adaptation patterns without heavy framework ceremony,
- providing first-party tooling (CLI, Gradle plugin, LSP, IntelliJ integration).

## Core concepts

LMF follows the classic modeling stack:

- **M3**: LMCore, the language definition itself.
- **M2**: your domain meta-models written in `.lm`.
- **M1**: your instance models (also in `.lm`, or created programmatically).

At runtime, models are loaded, linked, validated, and exposed through typed Java objects and framework services.

## Project structure

### `logoce.lmf.core.api`

The foundation module.
It contains LMCore and the core runtime APIs (`org.logoce.lmf.core.lang`, model abstractions, adapter/extender contracts, notification primitives, feature/model services).

This is where the language semantics and base runtime contracts live.

### `logoce.lmf.core.loader`

The parsing and linking layer.
It provides the `.lm` loader implementation (`ILmLoader` provider), diagnostics, linking utilities, and tooling-facing loading APIs.

This is the module that turns textual models into usable in-memory objects.

### `logoce.lmf.core.generator`

The Java code generator.
Given `.lm` meta-models, it generates strongly typed Java APIs and builders (using JavaPoet).

This closes the loop between textual modeling and type-safe Java development.

### `logoce.lmf.cli`

The command-line interface (`lm`).
It exposes model discovery, checking, formatting, tree inspection, references, and edit operations for `.lm` assets.

This is the primary day-to-day operator/developer tool for working with models directly.

### `logoce.lmf.gradle`

The Gradle plugin (`org.logoce.lmf.gradle-plugin`).
It integrates model generation into build pipelines and source sets so generated code is wired automatically into Java compilation.

This is the standard path for project-level integration.

### `logoce.lmf.lsp`

The Language Server Protocol implementation for `.lm`.
It provides editor-friendly diagnostics and language tooling over LSP4J.

This enables rich editor workflows beyond CLI usage.

### `logoce.lmf.intellij`

IntelliJ integration for `.lm` files.
It registers the file type and connects IntelliJ to the LMF LSP server for language features inside the IDE.

## How the parts work together

Typical flow:

1. Define a meta-model in `.lm` (M2).
2. Use the generator (directly or via Gradle plugin) to produce Java APIs.
3. Author instance models (M1) in `.lm`.
4. Load/link models through the loader at runtime.
5. Use adapters/extensions and generated types in application code.
6. Use CLI/LSP/IntelliJ tooling for validation, inspection, and maintenance.

## Related documentation in this repository

- `LMCORE_HOWTO.md`: practical guide for authoring LMCore `.lm` models.
- `LM_TOOL.md`: complete CLI usage for the `lm` command.
- `logoce.lmf.core.api/src/main/model/asset/LMCore.lm`: the LMCore language definition.
