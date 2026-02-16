# LM Tool (`lm`)

`lm` is a small command-line tool to load/check/format `.lm` models from a workspace.

## Install

From the workspace root:

```bash
./install-cli.sh
```

This builds the CLI distribution and installs:
- a launcher script to `~/.local/bin/lm`
- the CLI distribution to `~/.local/share/logoce-lmf-cli`

Optional environment overrides:
- `LMF_CLI_BIN_DIR` (default: `~/.local/bin`)
- `LMF_CLI_INSTALL_ROOT` (default: `~/.local/share/logoce-lmf-cli`)

## Usage

```text
lm [--project-root <path>] models [--duplicates] [--json]
lm [--project-root <path>] check <model.lm> [--constraints] [--json]
lm [--project-root <path>] check --all [--constraints] [--verbose] [--exclude <path|glob>]... [--json]
lm [--project-root <path>] fmt <model.lm> [--root <ref>] [--ref-path-to-name] [--syntax-only] [--in-place] [--json]
lm [--project-root <path>] tree <model.lm> [--root <ref>] [--max-depth <n>] [--always-index] [--syntax-only] [--json]
lm [--project-root <path>] features <model.lm> <objectRef> [--json]
lm [--project-root <path>] ref <model.lm> <ref> [--include-descendants] [--json]
lm [--project-root <path>] batch [--file <path> | --stdin] [--dry-run] [--continue-on-error] [--force] [--validate each|final|none] [--default-model <model.lm>] [--json]
lm [--project-root <path>] replace <model.lm> <ref> --subtree-file <path> [--force] [--json]
lm [--project-root <path>] replace <model.lm> <ref> --subtree-stdin [--force] [--json]
lm [--project-root <path>] replace <model.lm> <ref> - [--force] [--json]
lm [--project-root <path>] replace <model.lm> <ref> <subtree> [--force] [--json]
lm [--project-root <path>] remove <model.lm> <ref> [--json]
lm [--project-root <path>] insert <model.lm> <ref> --subtree-file <path> [--json]
lm [--project-root <path>] insert <model.lm> <ref> --subtree-stdin [--json]
lm [--project-root <path>] insert <model.lm> <ref> - [--json]
lm [--project-root <path>] insert <model.lm> <ref> <subtree> [--json]
lm [--project-root <path>] move <model.lm> <fromRef> <toRef> [--json]
lm [--project-root <path>] set <model.lm> <objectRef> <featureName> <value> [--json]
lm [--project-root <path>] unset <model.lm> <objectRef> <featureName> [--json]
lm [--project-root <path>] add <model.lm> <objectRef> <featureName> <value> [--json]
lm [--project-root <path>] remove-value <model.lm> <objectRef> <featureName> <value> [--json]
lm [--project-root <path>] clear <model.lm> <objectRef> <featureName> [--json]
lm [--project-root <path>] rename <model.lm> <ref> <newName> [--json]
```

`<model.lm>` can be:
- a path (relative to `--project-root`, or absolute)
- a file name (searched recursively under `--project-root`)
- a qualified name reference: `qn:<domain.name>` (for example `qn:test.model.CarCompany`)
- or use `--model <domain.name>` in place of the `<model.lm>` argument (shorthand for `qn:<domain.name>`)
- Tip: if the file name is not unique, `lm` reports an “Ambiguous model reference” and lists matches; use a relative/absolute path to disambiguate.
- Tip: use `lm models` to list models and their qualified names (and detect duplicates).

Exit codes:
- `0` OK
- `1` invalid model (parse/link errors)
- `2` usage error (unknown command/options, ambiguous model path, etc.)

`--json`:
- When supported by a command, prints a machine-readable JSON object to stdout (useful for agents).
- Contract: JSON is written to stdout; human messages and diagnostics are written to stderr. Do not merge streams (for example `2>&1`) if you need valid JSON.
- For edit commands, the JSON `outcome` separates `plannedFiles` (touched by the planned edit) from `writtenFiles` (actually written to disk; empty when `wrote=false`).
- For `batch --json`, the `finalization` section similarly separates `plannedFiles` vs `writtenFiles`.

`--project-root`:
- It defines the workspace boundary used for model discovery/import resolution (header scanning under that directory).
- Keep it as small as practical: any extra `.lm` files under it (scratch copies, snippets, vendored test data) can create “duplicate qualified name” / “ambiguous model” failures that affect otherwise unrelated commands.
- Strong recommendation: keep scratch files outside `--project-root`, or isolate them under a separate smaller `--project-root`.

Editing workflow (recommended):
- Use `lm tree --always-index` first to discover copy/pasteable object refs; crafting refs by hand is error-prone.
- Use `lm fmt --root <ref>` to inspect the exact subtree you intend to edit (useful when a containment “wraps” an inner list object).
- Use `lm features <model> <objectRef>` to understand whether a feature is an attribute, a containment, or a reference relation before choosing an edit command.
- Use `lm ref` before destructive edits (`remove`/`move`) to understand impact.

Formatting/whitespace:
- All edit commands format the touched files before writing (even for small edits), so diffs can include whitespace/structure changes outside the edited span.
- There is currently no “no-format” option; this is intentional to keep `.lm` sources normalized.
- The formatter is not a minimal-diff formatter; it may also reflow constructs into long lines. If you care about review diffs, format files up-front (for example `lm fmt --in-place`) and then do edits on already-normalized sources.

Validation (important):
- By default, `lm check` is strict about syntax + linking (import graph + reference resolution) only: it exits `0` when the model is linkable.
- `lm check --constraints` runs an additional constraint pass over the built object graph and emits warnings such as “Missing mandatory feature ceo ([1..1]) …”, but still exits `0` as long as the model links.
- Edit commands still validate like the default `check` (parse + link/import resolution), not constraints.

Agent-safe references:
- Prefer `lm tree --always-index` when you want copy/pasteable refs that stay unambiguous.
- Prefer `@name` anchors for long-lived targets when possible; `lm fmt --ref-path-to-name` can rewrite absolute paths into `@name` when it can prove the target name is unique.
- Avoid path references that omit list indices for list features (for example `/parcs/cars`): they resolve to the first element and can silently change meaning after list edits.

## Commands

### `models`

Lists discovered `.lm` models under `--project-root`, with their paths and qualified names.

Examples:

```bash
lm models
lm models --json
lm models --duplicates
lm models --duplicates --json
```

### `check`

Loads a model and prints diagnostics (if any). If there are no errors, prints `OK: <path>`.
With `--all`, scans all `.lm` files under `--project-root` and checks each one (errors-only by default, plus a final summary).
With `--verbose`, `check --all` also prints `OK: <path>` for each valid file.
With `--exclude <path|glob>`, `check --all` skips matching files/directories (repeatable).
With `--constraints`, runs an extra mandatory-feature pass and emits warnings (does not affect exit code as long as linking succeeds).
With `--json`, prints structured results to stdout.

Important note about `--exclude`:
- `--exclude` only affects which `.lm` files are *checked* by `check --all`.
- It does **not** prevent excluded files from being discovered by other workspace scans (import resolution / qualified-name lookup), so excluded duplicates can still cause ambiguity/duplicate-qualified-name errors (even when checking a single explicit file).
- If you need true isolation, use a smaller `--project-root` (or move scratch files outside the project root).

Examples:

```bash
lm check LMCore.lm
lm --project-root /path/to/workspace check src/main/model/asset/LMCore.lm
lm check --all
lm check --all --verbose
lm check --all --exclude tmp --exclude '**/snippets/**'
```

### `fmt`

Parses and prints a formatted version of the model to stdout.

Examples:

```bash
lm fmt LMCore.lm
lm fmt LMCore.lm > LMCore.formatted.lm
```

#### `fmt --in-place`

Rewrites the model file in-place with the formatted version (full file only; `--root` is not allowed).

Examples:

```bash
lm fmt LMCore.lm --in-place
lm fmt BrokenModel.lm --syntax-only --in-place
```

#### `fmt --json`

Outputs a JSON object to stdout (for agent consumption). When not using `--in-place`, the JSON includes a `formatted` string field.

#### `fmt --ref-path-to-name`

Rewrites absolute path references like `/groups.0` into named references like `@SomeName` when the target name is unambiguous for the referenced feature type (concept group). If it cannot prove the `@name` reference is unambiguous, the formatter keeps the original path.

Example:

```bash
lm fmt LMCore.lm --ref-path-to-name
```

#### `fmt --root`

Formats only a subtree selected by a reference (resolved against the loaded document).

Examples:

```bash
lm fmt LMCore.lm --root @GraphView
lm fmt LMCore.lm --root /groups.1
lm fmt LMCore.lm --root @SomeNode/../siblings.2
```

Notes:
- `--root` currently supports local references (for example `@name`, `/child.0`, `../parent`-style steps).
- Model-qualified references are not supported for `--root` (for example `#OtherModel@Type`).

#### `fmt --syntax-only`

Formats using only the parser (no linking). This is useful for inspecting or reformatting a model that does not currently link.

Notes:
- `--syntax-only` disables `--root` and `--ref-path-to-name`.

### `tree`

Lists all containment paths in a model. Each line is tab-separated:

```
<path>\t<group>\t<name>
```

Paths include `.0` when a containment has multiple children of the same relation name. When only one child exists for a relation, the `.0` is omitted.
Use `--root` to start from a subtree; traversal is restricted to the selected root, but printed paths remain full (copy/pasteable) references.
Use `--max-depth` (alias: `--depth`) to limit traversal depth.
`--root` accepts the same local reference syntax as `fmt --root`.
Use `--always-index` to always show `.0` for list containments (even when the list currently has one element).
Path references accept both `/x` and `/x.0` (they both mean “first element”).
Use `--syntax-only` to list the syntax tree without linking (no `--root` support; paths are syntax-based, not semantic references).
With `--json`, outputs a JSON object to stdout with an `items` array (`path`, `group`, `name`).

Example:

```bash
lm tree Application.vsand.lm --max-depth 3
lm tree Application.vsand.lm --root @GraphView
```

### `features`

Lists all features of a resolved object, including each feature kind:
- `attribute`: value stored on the object
- `contains`: containment relation (structural child object(s))
- `refers`: non-containment relation (reference to another object)

Output is tab-separated:

```
<featureName>\t<kind>\t<cardinality>\t<type>
```

Examples:

```bash
lm features ModelB.lm /barrier
lm features ModelB.lm /barrier/buffers
lm features ModelB.lm /barrier/buffers --json
```

Notes:
- `contains` features are edited structurally (`insert`/`remove`/`move`/`replace`), not with `set`/`add`.
- This command helps discover “wrapper” patterns where an outer containment holds an inner object that contains the list you actually want to edit. Use `lm fmt --root <ref>` to inspect the subtree and confirm you are targeting the correct object.

### `ref`

Finds resolved references to a specific node (within the target model), in the target model and in any model importing it.

Usage:

```bash
lm ref Application.vsand.lm /materials
lm ref Application.vsand.lm @Lava
lm ref Application.vsand.lm /materials --include-descendants
lm ref Application.vsand.lm /materials --json
```

Output is tab-separated:

```
<file>:<line>:<col>\t<rawReference>\t<resolvedModelQualifiedName><resolvedPath>\t<matchKind>
```

Notes:
- By default, only exact matches are printed. If there are no exact matches, the tool falls back to descendant matches (path-like references only) to avoid an empty result for list/container queries like `/materials`.
- `--include-descendants` also matches references to descendant objects of the target node, but only when the reference text is path-like (contains `/`).

### `replace`

Replaces a subtree in a model file in-place. The replacement is validated before writing; on any error, the tool prints diagnostics and **does not modify the file**.

Usage:

```bash
lm replace Application.vsand.lm /materials/materials.1 --subtree-file subtree.lm
cat subtree.lm | lm replace Application.vsand.lm /materials/materials.1 --subtree-stdin
cat subtree.lm | lm replace Application.vsand.lm /materials/materials.1 -
lm replace Application.vsand.lm /materials/materials.1 --subtree-stdin <<'LM'
(Material name="Stone \"Q\"")
LM
# Deprecated (one-liner only): inline <subtree> is shell-fragile; prefer --subtree-stdin/--subtree-file
lm replace Application.vsand.lm /materials/materials.1 "(Material name=Stone)"
```

Notes:
- Use exactly one subtree source: an inline `<subtree>`, `-`, `--subtree-file`, or `--subtree-stdin` (they are mutually exclusive).
- `<subtree>` must parse as exactly one root element.
- Inline `<subtree>` is **deprecated** (kept for now for simple one-liners); prefer `--subtree-file` or `--subtree-stdin` (or `<subtree>` = `-`) because shell quoting is fragile. It may be removed in a future version.
- `--subtree-file <path>` is resolved relative to `--project-root` unless `<path>` is absolute.
- Strong recommendation: keep subtree snippet files outside `--project-root` or use a different extension than `.lm`. `check --all --exclude` can avoid checking them, but excluded `.lm` can still influence workspace resolution in other commands.
- Without `--force`, the file is only written when the updated model has no errors.
- With `--force`, the file is written even if validation has errors (errors are still printed).
- With `--json`, prints a structured summary (files touched, outcome, etc.).

### `remove`

Removes a subtree from a model file in-place. The updated workspace is validated before writing; on any error, the tool prints diagnostics and **does not modify any file**.

Usage:

```bash
lm remove Application.vsand.lm /materials/materials.1
```

Behavior:
- The removed node is deleted from the target file.
- References to the removed node (and its subtree) are unset.
- Path-like references to later siblings in the same list are shifted (index − 1).
- Importer models are updated as well (any model that imports the target, directly or transitively).

Output:
```
<file>:<line>:<col>\t<rawReference>\t<resolvedModelQualifiedName><resolvedPath>\tunset
```

With `--json`, prints a structured summary (unset refs, shifted refs, files touched, outcome, etc.).

### `insert`

Inserts a subtree into a model file in-place. The updated workspace is validated before writing; on any error, the tool prints diagnostics and **does not modify any file**.

Usage:

```bash
lm insert Application.vsand.lm /materials/materials.1 --subtree-file subtree.lm
cat subtree.lm | lm insert Application.vsand.lm /materials/materials.1 --subtree-stdin
cat subtree.lm | lm insert Application.vsand.lm /materials/materials.1 -
lm insert Application.vsand.lm /materials/materials.1 --subtree-stdin <<'LM'
(Material name="Sand \"Q\"")
LM
# Deprecated (one-liner only): inline <subtree> is shell-fragile; prefer --subtree-stdin/--subtree-file
lm insert Application.vsand.lm /materials/materials.1 "(Material name=Sand)"
```

Behavior:
- When `<ref>` targets a containment list element (for example `/parent/items.3`), the subtree is inserted **before** that index (between index−1 and index). If the index equals the current list size, it appends to the end.
- When `<ref>` targets a single containment slot (for example `/settings`), the subtree is added only if the slot is empty; if already present, `insert` fails (use `replace` instead).
- Use exactly one subtree source: an inline `<subtree>`, `-`, `--subtree-file`, or `--subtree-stdin` (they are mutually exclusive).
- Inline `<subtree>` is **deprecated** (kept for now for simple one-liners); prefer `--subtree-file` or `--subtree-stdin` (or `<subtree>` = `-`) because shell quoting is fragile. It may be removed in a future version.
- `--subtree-file <path>` is resolved relative to `--project-root` unless `<path>` is absolute.

With `--json`, prints a structured summary (shifted refs, files touched, outcome, etc.).
- For list insertions, path-like references to later siblings in the same list are shifted (index + 1).
- Importer models are updated as well (any model that imports the target, directly or transitively).

### `move`

Moves a subtree from one location to another inside the same model file. The updated workspace is validated before writing; on any error, the tool prints diagnostics and **does not modify any file**.

Usage:

```bash
lm move Application.vsand.lm /materials/materials.1 /materials/materials.3
lm move Application.vsand.lm /settings /profiles.0
```

Behavior:
- `<fromRef>` must resolve to a single object in the target model.
- `<toRef>` uses the same slot syntax as `insert`:
  - list slot: `/parent/items.<index>` (insert **before** that index; `index == size` appends to the end)
  - single slot: `/parent/item` (must be empty; otherwise use `replace`)
- References to the moved subtree are updated (path-like refs are rewritten).
- Path-like references to affected siblings are shifted to keep indices consistent.
- Importer models are updated as well (any model that imports the target, directly or transitively).

With `--json`, prints a structured summary (files touched, outcome, and reference rewrites when available).

### `rename`

Renames a `#LMCore@Named` object by updating its `name` attribute (positional or `name=` assignment), and updates name-based references that point to it (for example `@OldName`, `#Model@OldName`, and their `/...` variants).

Usage:

```bash
lm rename Application.vsand.lm /materials/materials.1 Lava
lm rename Application.vsand.lm @Lava "Lava Boiling"
```

With `--json`, prints a structured summary (files touched and outcome).

### `set`

Sets (or updates) a non-containment feature on an object, in-place. The updated workspace is validated before writing; on any error, the tool prints diagnostics and **does not modify any file**.

Usage:

```bash
lm set Application.vsand.lm /materials/materials.1 name Sand
lm set Application.vsand.lm / mainMaterial /materials/materials.3
```

Notes:
- Containments are not supported (use `insert`/`replace`/`remove`).
- List features are supported, as a full replacement: pass comma-separated values (use `add` / `remove-value` for incremental edits).
- `<value>` is treated as a single LM value literal (parsed/validated as one value token).
  - If the value contains spaces (for example a ref like `@Mass Buffer Tmp`), pass it as a single CLI argument (shell-quote it), and `lm` will persist it as a quoted LM value in the file (for example `buffers="@Mass Buffer Tmp"`).

With `--json`, prints a structured summary (files touched and outcome).

### `unset`

Unsets an optional feature on an object, in-place. The updated workspace is validated before writing; on any error, the tool prints diagnostics and **does not modify any file**.

Usage:

```bash
lm unset Application.vsand.lm / mainMaterial
```

Notes:
- Mandatory features cannot be unset.
- Containments are not supported (use `remove`).
- For list features, `unset` removes the whole assignment (use `remove-value` to remove a single element, or `clear` as an explicit list-only alias).

With `--json`, prints a structured summary (files touched and outcome).

### `add`

Adds a single value to a list-valued, non-containment feature on an object. If the value is already present, it is a no-op.

Usage:

```bash
lm add MyModel.lm / imports other.domain.OtherModel
lm add MyModel.lm / metamodels other.domain.SomeMetaModel
lm add MyModel.lm /barrier/buffers buffers "@Mass Buffer Tmp"
```

Notes:
- Only supported on list-valued, non-containment features.
- To replace the whole list, use `set`.
- `<value>` is treated as a single LM value literal (parsed/validated as one value token).
  - If the value contains spaces, pass it as a single CLI argument (shell-quote it).

With `--json`, prints a structured summary (files touched and outcome).

### `remove-value`

Removes a single value from a list-valued, non-containment feature on an object. If the value is not present, it is a no-op.

Usage:

```bash
lm remove-value MyModel.lm / imports other.domain.OtherModel
lm remove-value MyModel.lm /barrier/buffers buffers "@Mass Buffer 2"
```

Notes:
- Only supported on list-valued, non-containment features.
- To remove the whole assignment, use `clear` or `unset`.
- `<value>` is treated as a single LM value literal (parsed/validated as one value token).
  - If the value contains spaces, pass it as a single CLI argument (shell-quote it).

With `--json`, prints a structured summary (files touched and outcome).

### `clear`

Clears a list-valued, non-containment feature by removing its whole assignment (list-only `unset`).

Usage:

```bash
lm clear MyModel.lm / imports
```

Notes:
- Only supported on list-valued, non-containment features.

With `--json`, prints a structured summary (files touched and outcome).

### `batch`

Runs a sequence of edit commands from a script (JSON Lines: one JSON object per line). The batch is staged in memory and written once at the end; if any operation fails, **no changes are written**.

Supported `cmd` values: `replace`, `remove`, `insert`, `move`, `set`, `unset`, `add`, `remove-value`, `clear`, `rename`.

Input examples:

```jsonl
{"cmd":"remove","args":["Application.vsand.lm","/materials/materials.1"]}
{"cmd":"rename","args":["Application.vsand.lm","@Lava","Lava Boiling"]}
```

Options:
- `--file <path>` read the JSONL script from a file (path is relative to `--project-root` unless absolute)
- `--stdin` read the JSONL script from stdin (default when no file is provided)
- `--default-model <model.lm>` allow omitting `<model.lm>` as the first arg for operations
- `--validate each|final|none` validation strategy (default: `final`)
- `--force` write even if validation has errors (still prints diagnostics, exit code is `1`)
- `--dry-run` do not write any file (still runs planning/validation)
- `--continue-on-error` keep executing operations after an error (still writes nothing if any operation failed)
- `--json` machine-readable output (JSON object to stdout)

Example using `--default-model`:

```jsonl
{"cmd":"remove","args":["/materials/materials.1"]}
{"cmd":"rename","args":["@Lava","Lava Boiling"]}
```

```bash
lm batch --file edits.jsonl --default-model Application.vsand.lm
```

Notes:
- `--force` is currently supported by `replace` and `batch` only.
- For multi-step refactors, prefer `batch` with `--validate final` (default) so validation happens only on the final state; use `--force` if you still want to write with errors.
- Warning about `--validate none`: this disables post-operation validation, but operations still need to load/link the workspace to plan edits. If you write a broken import graph or missing meta-models, `lm` may not be able to load the workspace to undo the change; prefer `--validate final` or `--validate each` unless you can recover manually.
