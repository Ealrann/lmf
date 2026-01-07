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
lm [--project-root <path>] check <model.lm>
lm [--project-root <path>] check --all
lm [--project-root <path>] fmt <model.lm> [--root <ref>] [--ref-path-to-name]
lm [--project-root <path>] tree <model.lm> [--root <ref>] [--max-depth <n>]
lm [--project-root <path>] ref <model.lm> <ref> [--include-descendants]
lm [--project-root <path>] batch [--file <path> | --stdin] [--dry-run] [--continue-on-error] [--force] [--validate each|final|none] [--default-model <model.lm>]
lm [--project-root <path>] replace <model.lm> <ref> <subtree> [--force]
lm [--project-root <path>] remove <model.lm> <ref>
lm [--project-root <path>] insert <model.lm> <ref> <subtree>
lm [--project-root <path>] move <model.lm> <fromRef> <toRef>
lm [--project-root <path>] set <model.lm> <objectRef> <featureName> <value>
lm [--project-root <path>] unset <model.lm> <objectRef> <featureName>
lm [--project-root <path>] rename <model.lm> <ref> <newName>
```

`<model.lm>` can be:
- a path (relative to `--project-root`, or absolute)
- a file name (searched recursively under `--project-root`)

Exit codes:
- `0` OK
- `1` invalid model (parse/link errors)
- `2` usage error (unknown command/options, ambiguous model path, etc.)

## Commands

### `check`

Loads a model and prints diagnostics (if any). If there are no errors, prints `OK: <path>`.
With `--all`, scans all `.lm` files under `--project-root` and checks each one.

Examples:

```bash
lm check LMCore.lm
lm --project-root /path/to/workspace check src/main/model/asset/LMCore.lm
lm check --all
```

### `fmt`

Parses and prints a formatted version of the model to stdout.

Examples:

```bash
lm fmt LMCore.lm
lm fmt LMCore.lm > LMCore.formatted.lm
```

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

### `tree`

Lists all containment paths in a model. Each line is tab-separated:

```
<path>\t<group>\t<name>
```

Paths include `.0` when a containment has multiple children of the same relation name. When only one child exists for a relation, the `.0` is omitted.
Use `--root` to start from a subtree; traversal is restricted to the selected root, but printed paths remain full (copy/pasteable) references.
Use `--max-depth` (alias: `--depth`) to limit traversal depth.
`--root` accepts the same local reference syntax as `fmt --root`.

Example:

```bash
lm tree Application.vsand.lm --max-depth 3
lm tree Application.vsand.lm --root @GraphView
```

### `ref`

Finds resolved references to a specific node (within the target model), in the target model and in any model importing it.

Usage:

```bash
lm ref Application.vsand.lm /materials
lm ref Application.vsand.lm @Lava
lm ref Application.vsand.lm /materials --include-descendants
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
lm replace Application.vsand.lm /materials/materials.1 "(Material name=Stone)"
```

Notes:
- `<subtree>` must parse as exactly one root element (wrap it in quotes).
- Without `--force`, the file is only written when the updated model has no errors.
- With `--force`, the file is written even if validation has errors (errors are still printed).

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

### `insert`

Inserts a subtree into a model file in-place. The updated workspace is validated before writing; on any error, the tool prints diagnostics and **does not modify any file**.

Usage:

```bash
lm insert Application.vsand.lm /materials/materials.1 "(Material name=Sand)"
```

Behavior:
- When `<ref>` targets a containment list element (for example `/parent/items.3`), the subtree is inserted **before** that index (between index−1 and index).
- When `<ref>` targets a single containment slot (for example `/settings`), the subtree is added only if the slot is empty; if already present, `insert` fails (use `replace` instead).
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
  - list slot: `/parent/items.<index>` (insert **before** that index)
  - single slot: `/parent/item` (must be empty; otherwise use `replace`)
- References to the moved subtree are updated (path-like refs are rewritten).
- Path-like references to affected siblings are shifted to keep indices consistent.
- Importer models are updated as well (any model that imports the target, directly or transitively).

### `rename`

Renames a `#LMCore@Named` object by updating its `name` attribute (positional or `name=` assignment), and updates name-based references that point to it (for example `@OldName`, `#Model@OldName`, and their `/...` variants).

Usage:

```bash
lm rename Application.vsand.lm /materials/materials.1 Lava
lm rename Application.vsand.lm @Lava "Lava Boiling"
```

### `set`

Sets (or updates) a single-valued, non-containment feature on an object, in-place. The updated workspace is validated before writing; on any error, the tool prints diagnostics and **does not modify any file**.

Usage:

```bash
lm set Application.vsand.lm /materials/materials.1 name Sand
lm set Application.vsand.lm / mainMaterial /materials/materials.3
```

Notes:
- Containments are not supported (use `insert`/`replace`/`remove`).
- List features are not supported yet.

### `unset`

Unsets a single-valued, optional feature on an object, in-place. The updated workspace is validated before writing; on any error, the tool prints diagnostics and **does not modify any file**.

Usage:

```bash
lm unset Application.vsand.lm / mainMaterial
```

Notes:
- Mandatory features cannot be unset.
- Containments are not supported (use `remove`).
- List features are not supported yet.

### `batch`

Runs a sequence of edit commands from a script (JSON Lines: one JSON object per line). The batch is staged in memory and written once at the end; if any operation fails, **no changes are written**.

Supported `cmd` values: `replace`, `remove`, `insert`, `move`, `set`, `unset`, `rename`.

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

Example using `--default-model`:

```jsonl
{"cmd":"remove","args":["/materials/materials.1"]}
{"cmd":"rename","args":["@Lava","Lava Boiling"]}
```

```bash
lm batch --file edits.jsonl --default-model Application.vsand.lm
```
