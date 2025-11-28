# LMF LSP Design Notes

This folder collects reference material and design notes to brief an LSP expert on the **LMF** stack and the `.lm` language, and to discuss requirements for a rich language server.

## Contents

- `LMCORE_HOWTO.md` – existing how‑to for writing `.lm` meta‑models.
- `models/` – a small corpus of `.lm` files (stored as `.lm.txt` for convenience):
  - `LMCore.lm.txt` – the LMCore meta‑model (M3), the language’s “standard library”.
  - `CarCompany.lm.txt` – simple, single‑model example (enums, groups, containment).
  - `GraphCore.lm.txt` / `GraphExtensions.lm.txt` / `GraphAnalysis.lm.txt` – multi‑model graph example using imports.
  - `NativeGenerics.lm.txt` / `OperationsGeneric.lm.txt` – examples of generics and operations.
- `lmf-project-overview.md` – high‑level overview of the LMF workspace and runtime.
- `lm-language-editor-challenges.md` – why `.lm` is hard for editors and LSPs.
- `lm-lsp-requirements.md` – desired capabilities of a future LSP server.

All code references in these docs use the current layout of this repository.
