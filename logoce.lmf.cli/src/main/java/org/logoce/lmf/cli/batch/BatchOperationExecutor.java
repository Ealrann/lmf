package org.logoce.lmf.cli.batch;

import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.assign.FeatureAssignmentPlanner;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.edit.SubtreeSpanLocator;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.format.LmFormatter;
import org.logoce.lmf.cli.format.LmSourceFormatter;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.insert.InsertPlanResult;
import org.logoce.lmf.cli.insert.InsertPlanner;
import org.logoce.lmf.cli.move.MovePlanResult;
import org.logoce.lmf.cli.move.MovePlanner;
import org.logoce.lmf.cli.remove.RemoveModelDocument;
import org.logoce.lmf.cli.remove.RemovePlanResult;
import org.logoce.lmf.cli.remove.RemovePlanner;
import org.logoce.lmf.cli.remove.RemoveUnsetReference;
import org.logoce.lmf.cli.remove.RemoveWorkspace;
import org.logoce.lmf.cli.rename.RenamePlanResult;
import org.logoce.lmf.cli.rename.RenamePlanner;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.ModelLocator;
import org.logoce.lmf.cli.workspace.ModelResolution;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BatchOperationExecutor
{
	private final BatchExecutionContext context;
	private final LmSourceFormatter sourceFormatter = new LmSourceFormatter();

	public BatchOperationExecutor(final BatchExecutionContext context)
	{
		this.context = Objects.requireNonNull(context, "context");
	}

	public BatchOperationResult execute(final BatchOperation operation)
	{
		final var cmd = operation.command();
		return switch (cmd)
		{
			case "rename" -> executeRename(operation);
			case "remove" -> executeRemove(operation);
			case "insert" -> executeInsert(operation);
			case "move" -> executeMove(operation);
			case "set" -> executeSet(operation);
			case "unset" -> executeUnset(operation);
			case "replace" -> executeReplace(operation);
			default ->
			{
				context.err().println(opPrefix(operation) + "Unknown command: " + cmd);
				context.err().println(opPrefix(operation) + "Raw: " + operation.rawLine());
				yield new BatchOperationResult(ExitCodes.USAGE, false);
			}
		};
	}

	private BatchOperationResult executeRename(final BatchOperation operation)
	{
		final var options = context.options();
		final var parsed = parseModelArgs(operation, options, 3, 2);
		if (parsed == null)
		{
			return invalidArgs(operation, options, "rename", "<model.lm> <ref> <newName>");
		}

		final var modelSpec = parsed.modelSpec();
		final var targetReference = parsed.args().get(0);
		final var newName = parsed.args().get(1);

		final var modelPath = resolveModelPath(operation, modelSpec);
		if (modelPath == null)
		{
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}

		final var workspace = loadWorkspace(operation, modelPath, true);
		if (workspace == null)
		{
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var planner = new RenamePlanner();
		final var planResult = planner.plan(workspace.documents(), targetReference, newName);
		if (planResult instanceof RenamePlanResult.Failure failure)
		{
			context.err().println(opPrefix(operation) + failure.message());
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var planned = ((RenamePlanResult.Success) planResult).edit();
		if (!planned.changed() || planned.editsByFile().isEmpty())
		{
			context.out().println(opPrefix(operation) + "OK: nothing to rename");
			return new BatchOperationResult(ExitCodes.OK, false);
		}

		final var updatedSources = context.editApplier().apply(planned.editsByFile(), workspace.documents().sourcesByPath());
		final var formattedSources = formatUpdatedSources(updatedSources);
		final var overlayUpdate = OverlayUpdate.apply(context.overlaySources(), formattedSources);

		final var validated = validateWorkspaceAfterStage(operation, workspace.prepared());
		if (!validated && !options.force())
		{
			overlayUpdate.rollback(context.overlaySources());
			context.err().println(opPrefix(operation) + "Validation failed; operation rolled back");
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		context.validationRoots().add(new BatchValidationRoot(modelPath, true));
		context.out().println(opPrefix(operation) + "STAGED: renamed " + targetReference);
		return new BatchOperationResult(ExitCodes.OK, !validated);
	}

	private BatchOperationResult executeRemove(final BatchOperation operation)
	{
		final var options = context.options();
		final var parsed = parseModelArgs(operation, options, 2, 1);
		if (parsed == null)
		{
			return invalidArgs(operation, options, "remove", "<model.lm> <ref>");
		}

		final var modelSpec = parsed.modelSpec();
		final var targetReference = parsed.args().getFirst();

		final var modelPath = resolveModelPath(operation, modelSpec);
		if (modelPath == null)
		{
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}

		final var workspace = loadWorkspace(operation, modelPath, true);
		if (workspace == null)
		{
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var removeWorkspace = toRemoveWorkspace(workspace.documents());
		if (removeWorkspace == null)
		{
			context.err().println(opPrefix(operation) + "Cannot build remove workspace");
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var planner = new RemovePlanner();
		final var planResult = planner.plan(removeWorkspace, targetReference);
		if (planResult instanceof RemovePlanResult.Failure failure)
		{
			context.err().println(opPrefix(operation) + failure.message());
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var planned = ((RemovePlanResult.Success) planResult).edit();
		final var updatedSources = context.editApplier().apply(planned.editsByFile(), workspace.documents().sourcesByPath());
		final var formattedSources = formatUpdatedSources(updatedSources);
		final var overlayUpdate = OverlayUpdate.apply(context.overlaySources(), formattedSources);

		final var validated = validateWorkspaceAfterStage(operation, workspace.prepared());
		if (!validated && !options.force())
		{
			overlayUpdate.rollback(context.overlaySources());
			context.err().println(opPrefix(operation) + "Validation failed; operation rolled back");
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		context.deferredOut().addAll(formatUnsets(context.projectRoot(), planned.unsets()));
		context.validationRoots().add(new BatchValidationRoot(modelPath, true));
		context.out().println(opPrefix(operation) + "STAGED: removed " + targetReference);
		return new BatchOperationResult(ExitCodes.OK, !validated);
	}

	private BatchOperationResult executeInsert(final BatchOperation operation)
	{
		final var options = context.options();
		final var parsed = parseModelArgs(operation, options, 3, 2);
		if (parsed == null)
		{
			return invalidArgs(operation, options, "insert", "<model.lm> <ref> <subtree>");
		}

		final var modelSpec = parsed.modelSpec();
		final var targetReference = parsed.args().get(0);
		final var subtree = parsed.args().get(1);

		final var formattedSubtree = formatSingleRootSubtree(subtree, context.err(), operation);
		if (formattedSubtree == null)
		{
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}

		final var modelPath = resolveModelPath(operation, modelSpec);
		if (modelPath == null)
		{
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}

		final var workspace = loadWorkspace(operation, modelPath, true);
		if (workspace == null)
		{
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var planner = new InsertPlanner();
		final var planResult = planner.plan(workspace.documents(), targetReference, formattedSubtree);
		if (planResult instanceof InsertPlanResult.Failure failure)
		{
			context.err().println(opPrefix(operation) + failure.message());
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var planned = ((InsertPlanResult.Success) planResult).edit();
		final var updatedSources = context.editApplier().apply(planned.editsByFile(), workspace.documents().sourcesByPath());
		final var formattedSources = formatUpdatedSources(updatedSources);
		final var overlayUpdate = OverlayUpdate.apply(context.overlaySources(), formattedSources);

		final var validated = validateWorkspaceAfterStage(operation, workspace.prepared());
		if (!validated && !options.force())
		{
			overlayUpdate.rollback(context.overlaySources());
			context.err().println(opPrefix(operation) + "Validation failed; operation rolled back");
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		context.validationRoots().add(new BatchValidationRoot(modelPath, true));
		context.out().println(opPrefix(operation) + "STAGED: inserted into " + targetReference);
		return new BatchOperationResult(ExitCodes.OK, !validated);
	}

	private BatchOperationResult executeMove(final BatchOperation operation)
	{
		final var options = context.options();
		final var parsed = parseModelArgs(operation, options, 3, 2);
		if (parsed == null)
		{
			return invalidArgs(operation, options, "move", "<model.lm> <fromRef> <toRef>");
		}

		final var modelSpec = parsed.modelSpec();
		final var fromReference = parsed.args().get(0);
		final var toReference = parsed.args().get(1);

		final var modelPath = resolveModelPath(operation, modelSpec);
		if (modelPath == null)
		{
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}

		final var workspace = loadWorkspace(operation, modelPath, true);
		if (workspace == null)
		{
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var planner = new MovePlanner();
		final var planResult = planner.plan(workspace.documents(), fromReference, toReference);
		if (planResult instanceof MovePlanResult.Failure failure)
		{
			context.err().println(opPrefix(operation) + failure.message());
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var planned = ((MovePlanResult.Success) planResult).edit();
		if (!planned.changed() || planned.editsByFile().isEmpty())
		{
			context.out().println(opPrefix(operation) + "OK: nothing to move");
			return new BatchOperationResult(ExitCodes.OK, false);
		}

		final var updatedSources = context.editApplier().apply(planned.editsByFile(), workspace.documents().sourcesByPath());
		final var formattedSources = formatUpdatedSources(updatedSources);
		final var overlayUpdate = OverlayUpdate.apply(context.overlaySources(), formattedSources);

		final var validated = validateWorkspaceAfterStage(operation, workspace.prepared());
		if (!validated && !options.force())
		{
			overlayUpdate.rollback(context.overlaySources());
			context.err().println(opPrefix(operation) + "Validation failed; operation rolled back");
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		context.validationRoots().add(new BatchValidationRoot(modelPath, true));
		context.out().println(opPrefix(operation) + "STAGED: moved " + fromReference + " to " + toReference);
		return new BatchOperationResult(ExitCodes.OK, !validated);
	}

	private BatchOperationResult executeSet(final BatchOperation operation)
	{
		final var options = context.options();
		final var parsed = parseModelArgs(operation, options, 4, 3);
		if (parsed == null)
		{
			return invalidArgs(operation, options, "set", "<model.lm> <objectRef> <featureName> <value>");
		}

		final var modelSpec = parsed.modelSpec();
		final var objectReference = parsed.args().get(0);
		final var featureName = parsed.args().get(1);
		final var value = parsed.args().get(2);

		final var modelPath = resolveModelPath(operation, modelSpec);
		if (modelPath == null)
		{
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}

		final var workspace = loadWorkspace(operation, modelPath, true);
		if (workspace == null)
		{
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var targetDoc = workspace.documents().targetDocument();
		final var linkRoots = RootReferenceResolver.collectLinkRoots(targetDoc.document().linkTrees());
		if (linkRoots.isEmpty())
		{
			context.err().println(opPrefix(operation) + "No link trees available for " + PathDisplay.display(context.projectRoot(), modelPath));
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var resolved = new RootReferenceResolver().resolve(linkRoots, objectReference);
		if (!(resolved instanceof RootReferenceResolver.Resolution.Found found))
		{
			context.err().println(opPrefix(operation) + referenceResolutionMessage(objectReference, resolved));
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}

		final TextEdits.TextEdit edit;
		try
		{
			edit = new FeatureAssignmentPlanner().planSet(found.node(), targetDoc.document().source(), featureName, value);
		}
		catch (RuntimeException e)
		{
			context.err().println(opPrefix(operation) + messageOf(e));
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var updatedSources = context.editApplier().apply(Map.of(targetDoc.path(), List.of(edit)),
															  workspace.documents().sourcesByPath());
		final var formattedSources = formatUpdatedSources(updatedSources);
		final var overlayUpdate = OverlayUpdate.apply(context.overlaySources(), formattedSources);

		final var validated = validateWorkspaceAfterStage(operation, workspace.prepared());
		if (!validated && !options.force())
		{
			overlayUpdate.rollback(context.overlaySources());
			context.err().println(opPrefix(operation) + "Validation failed; operation rolled back");
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		context.validationRoots().add(new BatchValidationRoot(modelPath, true));
		context.out().println(opPrefix(operation) + "STAGED: set " + featureName + " on " + objectReference);
		return new BatchOperationResult(ExitCodes.OK, !validated);
	}

	private BatchOperationResult executeUnset(final BatchOperation operation)
	{
		final var options = context.options();
		final var parsed = parseModelArgs(operation, options, 3, 2);
		if (parsed == null)
		{
			return invalidArgs(operation, options, "unset", "<model.lm> <objectRef> <featureName>");
		}

		final var modelSpec = parsed.modelSpec();
		final var objectReference = parsed.args().get(0);
		final var featureName = parsed.args().get(1);

		final var modelPath = resolveModelPath(operation, modelSpec);
		if (modelPath == null)
		{
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}

		final var workspace = loadWorkspace(operation, modelPath, true);
		if (workspace == null)
		{
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var targetDoc = workspace.documents().targetDocument();
		final var linkRoots = RootReferenceResolver.collectLinkRoots(targetDoc.document().linkTrees());
		if (linkRoots.isEmpty())
		{
			context.err().println(opPrefix(operation) + "No link trees available for " + PathDisplay.display(context.projectRoot(), modelPath));
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var resolved = new RootReferenceResolver().resolve(linkRoots, objectReference);
		if (!(resolved instanceof RootReferenceResolver.Resolution.Found found))
		{
			context.err().println(opPrefix(operation) + referenceResolutionMessage(objectReference, resolved));
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}

		final TextEdits.TextEdit edit;
		try
		{
			edit = new FeatureAssignmentPlanner().planUnset(found.node(), targetDoc.document().source(), featureName);
		}
		catch (RuntimeException e)
		{
			context.err().println(opPrefix(operation) + messageOf(e));
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		if (edit == null)
		{
			context.out().println(opPrefix(operation) + "OK: nothing to unset");
			return new BatchOperationResult(ExitCodes.OK, false);
		}

		final var updatedSources = context.editApplier().apply(Map.of(targetDoc.path(), List.of(edit)),
															  workspace.documents().sourcesByPath());
		final var formattedSources = formatUpdatedSources(updatedSources);
		final var overlayUpdate = OverlayUpdate.apply(context.overlaySources(), formattedSources);

		final var validated = validateWorkspaceAfterStage(operation, workspace.prepared());
		if (!validated && !options.force())
		{
			overlayUpdate.rollback(context.overlaySources());
			context.err().println(opPrefix(operation) + "Validation failed; operation rolled back");
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		context.validationRoots().add(new BatchValidationRoot(modelPath, true));
		context.out().println(opPrefix(operation) + "STAGED: unset " + featureName + " on " + objectReference);
		return new BatchOperationResult(ExitCodes.OK, !validated);
	}

	private BatchOperationResult executeReplace(final BatchOperation operation)
	{
		final var options = context.options();
		final var parsed = parseModelArgs(operation, options, 3, 2);
		if (parsed == null)
		{
			return invalidArgs(operation, options, "replace", "<model.lm> <ref> <subtree>");
		}

		final var modelSpec = parsed.modelSpec();
		final var targetReference = parsed.args().get(0);
		final var replacementSubtree = parsed.args().get(1);

		final var modelPath = resolveModelPath(operation, modelSpec);
		if (modelPath == null)
		{
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}

		final var prepareResult = context.registryService().prepareForModel(modelPath, context.err());
		if (prepareResult instanceof RegistryService.PrepareResult.Failure)
		{
			context.err().println(opPrefix(operation) + "Cannot prepare model registry for replace");
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var prepared = ((RegistryService.PrepareResult.Success) prepareResult).registry();
		final var registry = prepared.registry();
		final var targetQualifiedName = prepared.targetQualifiedName();

		final var originalSource = context.documentLoader().readString(modelPath, context.err());
		if (originalSource == null)
		{
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var originalDocument = context.documentLoader().loadModelFromSource(registry,
																	 targetQualifiedName,
																	 originalSource,
																	 context.err());
		final var linkRoots = RootReferenceResolver.collectLinkRoots(originalDocument.linkTrees());
		if (linkRoots.isEmpty())
		{
			context.err().println(opPrefix(operation) + "No link trees available for " + PathDisplay.display(context.projectRoot(), modelPath));
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var resolution = new RootReferenceResolver().resolve(linkRoots, targetReference);
		if (resolution instanceof RootReferenceResolver.Resolution.Ambiguous ambiguous)
		{
			context.err().println(opPrefix(operation) + "Ambiguous reference: " + targetReference);
			for (final var candidate : ambiguous.candidates())
			{
				context.err().println(opPrefix(operation) + " - " + candidate);
			}
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			context.err().println(opPrefix(operation) + "Cannot resolve reference: " + targetReference);
			context.err().println(opPrefix(operation) + notFound.message());
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			context.err().println(opPrefix(operation) + "Cannot resolve reference: " + targetReference);
			context.err().println(opPrefix(operation) + failure.message());
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}
		if (!(resolution instanceof RootReferenceResolver.Resolution.Found found))
		{
			context.err().println(opPrefix(operation) + "Unexpected reference resolution state");
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var span = SubtreeSpanLocator.locate(originalSource, found.node());
		if (span == null)
		{
			context.err().println(opPrefix(operation) + "Cannot locate subtree span for reference: " + targetReference);
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		final var formattedReplacement = formatSingleRootSubtree(replacementSubtree, context.err(), operation);
		if (formattedReplacement == null)
		{
			return new BatchOperationResult(ExitCodes.USAGE, false);
		}

		final var baseIndent = trailingIndentBefore(originalSource, span.startOffset());
		final var indentedReplacement = baseIndent.isEmpty()
									? formattedReplacement
									: formattedReplacement.replace("\n", "\n" + baseIndent);

		final var updatedSource = TextEdits.apply(originalSource,
												  List.of(new TextEdits.TextEdit(span.startOffset(),
																			   span.length(),
																			   indentedReplacement)));
		final var formattedSource = sourceFormatter.formatOrOriginal(updatedSource);

		final var validatedDocument = context.documentLoader().loadModelFromSource(registry,
																				   targetQualifiedName,
																				   formattedSource,
																				   context.err());
		final var diagnostics = validatedDocument.diagnostics();
		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			DiagnosticReporter.printDiagnostics(context.err(),
											 PathDisplay.display(context.projectRoot(), modelPath),
											 diagnostics);
			if (!options.force())
			{
				context.err().println(opPrefix(operation) + "Validation failed; operation not staged");
				return new BatchOperationResult(ExitCodes.INVALID, false);
			}
		}

		final var overlayUpdate = OverlayUpdate.apply(context.overlaySources(),
													 Map.of(modelPath.toAbsolutePath().normalize(), formattedSource));
		final boolean validated = !DiagnosticReporter.hasErrors(diagnostics);
		if (!validated && options.validateMode() == BatchOptions.ValidateMode.EACH && !options.force())
		{
			overlayUpdate.rollback(context.overlaySources());
			context.err().println(opPrefix(operation) + "Validation failed; operation rolled back");
			return new BatchOperationResult(ExitCodes.INVALID, false);
		}

		context.validationRoots().add(new BatchValidationRoot(modelPath, false));
		context.out().println(opPrefix(operation) + "STAGED: replaced " + targetReference);
		return new BatchOperationResult(ExitCodes.OK, !validated);
	}

	private static String formatSingleRootSubtree(final String subtreeSource,
									  final PrintWriter err,
									  final BatchOperation operation)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var reader = new LmTreeReader();
		final var readResult = reader.read(subtreeSource, diagnostics);

		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			err.println(opPrefix(operation) + "Subtree cannot be parsed");
			DiagnosticReporter.printDiagnostics(err, "<subtree>", diagnostics);
			return null;
		}

		if (readResult.roots().size() != 1)
		{
			err.println(opPrefix(operation) + "Subtree must contain exactly one root element; found: " + readResult.roots().size());
			return null;
		}

		return new LmFormatter().format(readResult.roots());
	}

	private record WorkspaceLoad(RegistryService.PreparedWorkspace prepared,
								org.logoce.lmf.cli.workspace.WorkspaceDocuments documents)
	{
	}

	private WorkspaceLoad loadWorkspace(final BatchOperation operation,
								final Path modelPath,
								final boolean requireValidImports)
	{
		final var projectRoot = context.projectRoot();
		final var displayPath = PathDisplay.display(projectRoot, modelPath);
		final var err = context.err();

		final var prepareResult = context.registryService().prepareForModelAndImporters(modelPath, err, requireValidImports);
		if (prepareResult instanceof RegistryService.PrepareWorkspaceResult.Failure failure)
		{
			err.println(opPrefix(operation) + "Cannot prepare workspace (exit " + failure.exitCode() + ")");
			return null;
		}

		final var prepared = ((RegistryService.PrepareWorkspaceResult.Success) prepareResult).workspace();
		final var registry = prepared.registry();
		final var documents = context.documentsLoader().load(context.documentLoader(),
															 registry,
															 prepared,
															 projectRoot,
															 err,
															 displayPath);
		if (documents == null || documents.targetDocument() == null)
		{
			err.println(opPrefix(operation) + "Cannot load workspace documents for " + displayPath);
			return null;
		}

		return new WorkspaceLoad(prepared, documents);
	}

	private static RemoveWorkspace toRemoveWorkspace(final org.logoce.lmf.cli.workspace.WorkspaceDocuments workspace)
	{
		final var targetPath = workspace.targetDocument().path();
		final var documents = new ArrayList<RemoveModelDocument>();
		RemoveModelDocument target = null;

		for (final var doc : workspace.documents())
		{
			final var modelDoc = new RemoveModelDocument(doc.path(), doc.document());
			documents.add(modelDoc);
			if (doc.path().equals(targetPath))
			{
				target = modelDoc;
			}
		}

		if (target == null)
		{
			return null;
		}
		return new RemoveWorkspace(target, List.copyOf(documents));
	}

	private static List<String> formatUnsets(final Path projectRoot, final List<RemoveUnsetReference> unsets)
	{
		if (unsets.isEmpty())
		{
			return List.of();
		}

		final var lines = new ArrayList<String>(unsets.size());
		for (final var unset : unsets)
		{
			final var location = formatLocation(projectRoot, unset.path(), unset.span());
			final var resolved = unset.targetId().modelQualifiedName() + unset.targetId().path();
			lines.add(location + "\t" + unset.raw() + "\t" + resolved + "\tunset");
		}
		return List.copyOf(lines);
	}

	private static String formatLocation(final Path projectRoot, final Path path, final TextPositions.Span span)
	{
		final var rel = PathDisplay.display(projectRoot, path);
		if (span == null)
		{
			return rel;
		}
		return rel + ":" + span.line() + ":" + span.column();
	}

	private static String opPrefix(final BatchOperation operation)
	{
		return "[batch:" + operation.index() + " line " + operation.lineNumber() + "] ";
	}

	private static String messageOf(final Exception e)
	{
		return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
	}

	private Path resolveModelPath(final BatchOperation operation, final String modelSpec)
	{
		final var cliContext = context.cliContext();
		final var locator = new ModelLocator(cliContext.projectRoot());
		final var resolution = locator.resolve(modelSpec);

		if (resolution instanceof ModelResolution.Found found)
		{
			return found.path();
		}
		if (resolution instanceof ModelResolution.Ambiguous ambiguous)
		{
			final var err = cliContext.err();
			err.println(opPrefix(operation) + "Ambiguous model reference: " + modelSpec);
			for (final var path : ambiguous.matches())
			{
				err.println(opPrefix(operation) + " - " + PathDisplay.display(cliContext.projectRoot(), path));
			}
			return null;
		}
		if (resolution instanceof ModelResolution.NotFound notFound)
		{
			final var err = cliContext.err();
			err.println(opPrefix(operation) + "Model not found: " + notFound.requested());
			err.println(opPrefix(operation) + "Searched under: " + cliContext.projectRoot());
			return null;
		}
		if (resolution instanceof ModelResolution.Failed failed)
		{
			final var err = cliContext.err();
			err.println(opPrefix(operation) + "Failed to search for model: " + failed.message());
			return null;
		}

		cliContext.err().println(opPrefix(operation) + "Unexpected model resolution state");
		return null;
	}

	private record ParsedModelArgs(String modelSpec, List<String> args)
	{
	}

	private static ParsedModelArgs parseModelArgs(final BatchOperation operation,
										final BatchOptions options,
										final int argsWithModel,
										final int argsWithDefaultModel)
	{
		final var provided = operation.args();
		if (provided.size() == argsWithModel)
		{
			return new ParsedModelArgs(provided.getFirst(), provided.subList(1, provided.size()));
		}

		final var defaultModel = options.defaultModel();
		if (defaultModel != null && !defaultModel.isBlank() && provided.size() == argsWithDefaultModel)
		{
			return new ParsedModelArgs(defaultModel, provided);
		}

		return null;
	}

	private BatchOperationResult invalidArgs(final BatchOperation operation,
									final BatchOptions options,
									final String command,
									final String usageTail)
	{
		final var err = context.err();
		err.println(opPrefix(operation) + "Invalid args for '" + command + "'");
		err.println(opPrefix(operation) + "Expected: " + usageTail);
		if (options.defaultModel() != null && !options.defaultModel().isBlank())
		{
			err.println(opPrefix(operation) + "Tip: args may omit <model.lm> because --default-model is set to " + options.defaultModel());
		}
		err.println(opPrefix(operation) + "Raw: " + operation.rawLine());
		return new BatchOperationResult(ExitCodes.USAGE, false);
	}

	private boolean validateWorkspaceAfterStage(final BatchOperation operation, final RegistryService.PreparedWorkspace prepared)
	{
		final var options = context.options();
		if (options.validateMode() != BatchOptions.ValidateMode.EACH)
		{
			return true;
		}

		final boolean ok = context.validator().validate(prepared,
										context.projectRoot(),
										context.overlaySources(),
										context.documentLoader(),
										context.err());
		if (!ok && options.force())
		{
			context.err().println(opPrefix(operation) + "FORCED: staged changes despite validation errors");
		}
		return ok;
	}

	private Map<Path, String> formatUpdatedSources(final Map<Path, String> updatedSources)
	{
		if (updatedSources.isEmpty())
		{
			return Map.of();
		}
		return sourceFormatter.formatAll(updatedSources);
	}

	private static String trailingIndentBefore(final CharSequence source, final int offset)
	{
		int start = offset;
		while (start > 0)
		{
			final char c = source.charAt(start - 1);
			if (c == ' ' || c == '\t')
			{
				start--;
				continue;
			}
			break;
		}
		return source.subSequence(start, offset).toString();
	}

	private static String referenceResolutionMessage(final String reference, final RootReferenceResolver.Resolution resolution)
	{
		if (resolution instanceof RootReferenceResolver.Resolution.Ambiguous ambiguous)
		{
			return "Ambiguous reference: " + reference + " (" + ambiguous.candidates().size() + " matches)";
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			return "Cannot resolve reference: " + notFound.message();
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			return "Cannot resolve reference: " + failure.message();
		}
		return "Cannot resolve reference: " + reference;
	}

	private record OverlayUpdate(Map<Path, String> previousByPath)
	{
		static OverlayUpdate apply(final Map<Path, String> overlaySources, final Map<Path, String> updatedSources)
		{
			final var previous = new LinkedHashMap<Path, String>();
			for (final var entry : updatedSources.entrySet())
			{
				final var normalized = entry.getKey().toAbsolutePath().normalize();
				previous.put(normalized, overlaySources.get(normalized));
				overlaySources.put(normalized, entry.getValue() == null ? "" : entry.getValue());
			}
			return new OverlayUpdate(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(previous)));
		}

		void rollback(final Map<Path, String> overlaySources)
		{
			for (final var entry : previousByPath.entrySet())
			{
				if (entry.getValue() == null)
				{
					overlaySources.remove(entry.getKey());
				}
				else
				{
					overlaySources.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}
}
