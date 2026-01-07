# LM Tool — `lm remove` Design & Implementation Plan

## Goal

Implement a new CLI mode:

```bash
lm remove <model.lm> <ref>
```

that deletes the subtree designated by `<ref>` from `<model.lm>`, updates *all affected references* in the workspace (including models that import the edited model), validates the result, and only then writes the modified files to disk as one atomic workspace transaction.

The command must be reliable for an AI coding agent:
- edits must be derived from *semantic resolution* (not string search),
- rewrite must be span-accurate (edit the exact token ranges),
- the workspace must never be left half-written (all-or-nothing write).

## Required Behavior

1) **Unset references to the removed object**
- If another object references the removed object (or anything inside the removed subtree), the command unsets that reference.
- The command prints all reference occurrences that were unset (so the user/agent can review what changed).

2) **Fix list index path references after deletion**
- If the removed object is inside a `many` containment (list), deleting it shifts sibling indices.
- Any *path-like* reference that targets a sibling with index `> removedIndex` must be rewritten (index − 1), including descendants under that sibling.
- This must *not* silently retarget references to “whatever is now at that index”.

3) **Update importer models**
- The rewrite applies not only to the edited model file, but also to any model that imports it (transitively, i.e., importers closure).

4) **Validation gate**
- Before writing files, the CLI validates that the modified workspace is linkable.
- If validation fails: print diagnostics and clearly state that no modifications were written.
- (Optional later) add `--force` like `replace` to write despite errors.

## CLI Interface (first version)

```bash
lm remove <model.lm> <ref>
```

- `<model.lm>` is resolved under `--project-root` like other commands.
- `<ref>` uses the same reference syntax already supported by `fmt/tree/ref/replace` (`/path`, `@name`, `@name/child.1`, `../`, `./`, `^contextName`, `#Model@Name/...` where supported by the resolver).

## Architecture (keep CLI thin, isolate the edit logic)

### New packages/classes in `logoce.lmf.cli`

- `org.logoce.lmf.cli.command.RemoveCommand`
  - Parse args only; delegate to runner.
- `org.logoce.lmf.cli.remove.RemoveRunner`
  - Orchestrates: resolve model path → prepare registries/workspace → compute edits → validate → commit.
- `org.logoce.lmf.cli.remove.RemovePlanner`
  - Pure “compute a workspace edit plan” component.
  - Returns `PlannedWorkspaceEdit` (edits per file + reports).
- `org.logoce.lmf.cli.remove.PlannedWorkspaceEdit`
  - `Map<Path, List<TextEdits.TextEdit>> editsByFile`
  - `List<UnsetReportLine> unsets`
  - (optional) counts: shifted refs, removed span info, touched files.

### Reuse existing CLI infrastructure

- Workspace scanning + registry building: `org.logoce.lmf.cli.workspace.RegistryService`, `DocumentLoader`
- Atomic multi-file writes: `org.logoce.lmf.cli.edit.WorkspaceWriteTransaction`
- Subtree span location: `org.logoce.lmf.cli.edit.SubtreeSpanLocator`
- Text patching: `org.logoce.lmf.cli.edit.TextEdits`
- Reference resolution for the remove target: `org.logoce.lmf.cli.format.RootReferenceResolver`

### One missing piece: span-accurate reference/token location

To safely edit references, we need to locate the exact token range of a specific reference raw value inside a node’s text.

Add a new utility:
- `org.logoce.lmf.cli.edit.FeatureValueSpanIndex` (name flexible)
  - Input: `PNode.tokens()`, `CharSequence source`
  - Output: an index mapping each parsed `PFeature` (name + values) to value spans (offset/length) and also the span of the whole `feature=value[,value...]` assignment.
  - This must mimic `TokenParser` behavior (whitespace, commas, quoted values), but *preserve offsets*.

This single utility prevents the “string search in node slice” approach (currently used in `lm ref`) from leaking into `remove`.

## Workspace model set to edit

`remove` needs both:
- the **target model** (the file we delete from), and
- the **importers closure** (files where we may need to unset/shift references).

Implementation plan:
- Extend `RegistryService` with “prepare for model + importers”:
  - scan: `DiskModelHeaderIndex.refresh(projectRoot)`
  - determine `targetQualifiedName` from header
  - compute:
    - `scanModels = importersClosure(targetQualifiedName)` (target + transitive importers)
    - `registryModels = importsClosure(scanModels)` (everything needed to link/validate)
  - resolve unique paths for all these qualified names (fail on ambiguity)
  - load meta-models closure via `DiskMetaModelHeaderIndex` + `LmWorkspace.loadMetaModels(...)`
  - build a dependency registry in import order (using `DocumentLoader`) so linking is stable

This avoids duplicating the ad-hoc closure logic currently living in `RefRunner`.

## Planner algorithm (semantic-first, then text edits)

### Step 1 — Load target document and resolve the remove target

- Load the target model document with `DocumentLoader.loadModelFromSource(registry, qn, source, err)`.
- Build link roots: `RootReferenceResolver.collectLinkRoots(doc.linkTrees())`.
- Resolve `<ref>` with `RootReferenceResolver.resolve(roots, ref)`:
  - must be exactly one `LinkNodeInternal`
  - compute `removedObjectId = ObjectId.from(node.build())`

### Step 2 — Determine list-shift context (if any)

If the node is contained by a `many` relation:
- identify:
  - `parentNode = targetNode.parent()`
  - `relation = targetNode.containingRelation()`
  - `featureName = relation.name()`
  - `removedIndex = index among siblings under the same containing relation`
  - `containerPath = ObjectId.from(parentNode.build()).path()`
- references that point into `containerPath + "/" + featureName + "." + i` with `i > removedIndex` must be shifted.

If not a `many` containment: no index-shift fixes are necessary.

### Step 3 — Collect semantic IDs for the removed subtree (safety)

Traverse `targetNode.streamTree()`:
- for each node that can `build()`, collect `ObjectId`
- build `removedIds` set and/or a prefix predicate:
  - exact match for removed root
  - descendant match for children

This ensures we don’t keep references to objects that will no longer exist.

### Step 4 — Scan all affected models and compute reference fixes

For each model in `scanModels` (target + importers):
- load its linked document (must be linkable for correctness; if not, `remove` should fail early unless `--force` exists)
- for each `LinkNodeInternal` in each link tree:
  - iterate `node.relationResolutions()`
  - for each `RelationReferences.resolved(attempt)`:
    - `raw = resolved.raw()`
    - `target = resolved.target()`
    - `targetId = ObjectId.from(target)`

Classify each occurrence:
- **UNSET** if `targetId` is in the removed subtree (removed root or any descendant).
- **SHIFT** if:
  - list-shift is enabled (removed from a `many` containment),
  - `targetId.modelQualifiedName == removedObjectId.modelQualifiedName`,
  - and `targetId.path()` matches the list container path pattern with an index `> removedIndex`,
  - and the raw reference is path-like (contains a path segment with indices). Named-only refs don’t need shifting.

### Step 5 — Turn semantic fixes into concrete `TextEdit`s

For each node containing an occurrence to fix:
- Build `FeatureValueSpanIndex` from `node.pNode().tokens()` and `source`.
- Find the exact value span corresponding to the `raw` reference string (and which feature assignment it belongs to).

Apply edits:

**UNSET**
- If the relation is single-valued: delete the whole `feature=value` assignment span.
- If list-valued (comma-separated): delete only that value and adjust separators/whitespace.
- If the list becomes empty after removal: delete the whole assignment.
- Record an `UnsetReportLine` with `path:line:col`, raw text, and resolved target id.

**SHIFT**
- Rewrite the raw reference string by decrementing the relevant `featureName.<index>` segment when it points under the shifted list.
- Apply a `TextEdit` on the exact value span.

### Step 6 — Delete the subtree in the target file

- Locate the exact `( ... )` subtree span with `SubtreeSpanLocator.locate(targetSource, targetNode)`.
- Delete that span.
- (Recommended) tune the deletion to avoid leaving trailing empty indentation lines.

### Step 7 — Produce the per-file planned workspace edits

- Group edits by file path.
- Ensure edits are non-overlapping; apply in descending offset order via `TextEdits.apply(...)`.
- The edit plan includes:
  - target model file (always)
  - any importer model where a reference was unset or shifted

## Validation & Commit

### Validation gate (before writing)

Validate the *modified* workspace using in-memory updated sources:
- rebuild/load models in dependency order using the same registry-building approach as `RegistryService`,
  but with “source overrides” for modified files (updated text instead of reading from disk).
- load/link each modified document with `DocumentLoader.loadModelFromSource(...)`.

If any document has errors:
- print diagnostics
- print `No changes written to <file>` (for each touched file or for the main target; keep consistent with other commands)
- abort without writing

### Atomic write

If validation passes:
- create a `WorkspaceWriteTransaction`
- `put(path, updatedSource)` for every modified file
- `commit(err)` once
- print:
  - summary (`OK: removed <ref> from <file>`, touched files count)
  - list of unsets (one per line, TSV)

## Output format (practical for an AI agent)

Proposed (TSV) per unset occurrence:

```
<file:line:col>\t<raw>\t<resolvedModelQN><resolvedPath>\tunset
```

Optionally add a final summary line:
- `UNSET: <count>  SHIFT: <count>  FILES: <count>`

## Tests (JUnit)

Add `logoce.lmf.cli/src/test/java/org/logoce/lmf/cli/RemoveRunnerTest.java`:

Workspace setup:
- a minimal meta-model with:
  - a `many` containment list (for index shift),
  - a `refers` relation pointing to list elements.
- `ModelA.lm` containing 3 list elements; references:
  - one reference to the removed element (must be unset),
  - one reference to a later sibling (must be shifted).
- `ModelB.lm` importing `ModelA` and referencing `#ModelA/...` (must be updated/unset).

Assertions:
- Files updated only when validation passes.
- Correct raw reference rewrites (shift).
- Correct unsets (assignment removed or list element removed).
- Console output includes the unset report lines.

## Implementation Phases (suggested order)

1) Refactor workspace closure logic into `RegistryService` (importers closure + combined prepare).
2) Implement `FeatureValueSpanIndex` and unit-test it on representative token patterns (single value, list values, quoted values).
3) Implement `RemovePlanner` (pure computation) + tests for planning outputs (edits + unsets).
4) Implement `RemoveRunner` + `RemoveCommand` + CLI wiring.
5) Add end-to-end `RemoveRunnerTest` using `@TempDir`.
6) Update `LM_TOOL.md` with the new `remove` mode usage and semantics.
