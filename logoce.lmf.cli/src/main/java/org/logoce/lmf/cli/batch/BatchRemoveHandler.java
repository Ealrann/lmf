package org.logoce.lmf.cli.batch;

import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.remove.RemoveModelDocument;
import org.logoce.lmf.cli.remove.RemovePlanResult;
import org.logoce.lmf.cli.remove.RemovePlanner;
import org.logoce.lmf.cli.remove.RemoveUnsetReference;
import org.logoce.lmf.cli.remove.RemoveWorkspace;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class BatchRemoveHandler implements BatchOperationHandler
{
	private final BatchOperationSupport support;

	BatchRemoveHandler(final BatchOperationSupport support)
	{
		this.support = Objects.requireNonNull(support, "support");
	}

	@Override
	public BatchOperationResult execute(final BatchOperation operation)
	{
		final var context = support.context();
		final var options = context.options();
		final var parsed = support.parseModelArgs(operation, options, 2, 1);
		if (parsed == null)
		{
			return support.invalidArgs(operation, options, "remove", "<model.lm> <ref>");
		}

		final var modelSpec = parsed.modelSpec();
		final var targetReference = parsed.args().getFirst();

		final var modelPath = support.resolveModelPath(operation, modelSpec);
		if (modelPath == null)
		{
			return support.result(operation,
							  ExitCodes.USAGE,
							  false,
							  "error",
							  "Cannot resolve model: " + modelSpec,
							  List.of(),
							  List.of());
		}

		final var workspace = support.loadWorkspace(operation, modelPath, true);
		if (workspace == null)
		{
			return support.result(operation,
							  ExitCodes.INVALID,
							  false,
							  "error",
							  "Cannot load workspace for " + modelSpec,
							  List.of(),
							  List.of());
		}

		final var removeWorkspace = toRemoveWorkspace(workspace.documents());
		if (removeWorkspace == null)
		{
			context.err().println(support.opPrefix(operation) + "Cannot build remove workspace");
			return support.result(operation,
							  ExitCodes.INVALID,
							  false,
							  "error",
							  "Cannot build remove workspace",
							  List.of(),
							  List.of());
		}

		final var planner = new RemovePlanner();
		final var planResult = planner.plan(removeWorkspace, targetReference);
		if (planResult instanceof RemovePlanResult.Failure failure)
		{
			context.err().println(support.opPrefix(operation) + failure.message());
			return support.result(operation,
							  ExitCodes.INVALID,
							  false,
							  "error",
							  failure.message(),
							  List.of(),
							  List.of());
		}

		final var planned = ((RemovePlanResult.Success) planResult).edit();
		final var updatedSources = context.editApplier().apply(planned.editsByFile(), workspace.documents().sourcesByPath());
		final var formattedSources = support.formatUpdatedSources(updatedSources);
		final var overlayUpdate = BatchOperationSupport.OverlayUpdate.apply(context.overlaySources(), formattedSources);

		final var stageValidation = support.validateWorkspaceAfterStage(operation, workspace.prepared(), formattedSources);
		if (!stageValidation.parseable())
		{
			overlayUpdate.rollback(context.overlaySources());
			context.err().println(support.opPrefix(operation) + "Parse error; operation rolled back");
			return support.result(operation,
							  ExitCodes.INVALID,
							  false,
							  "error",
							  "Parse error; operation rolled back",
							  List.of(),
							  stageValidation.report().diagnostics(),
							  List.of(),
							  List.of());
		}

		final var validated = stageValidation.validationPassed();
		if (!validated && !options.force())
		{
			overlayUpdate.rollback(context.overlaySources());
			context.err().println(support.opPrefix(operation) + "Validation failed; operation rolled back");
			return support.result(operation,
							  ExitCodes.INVALID,
							  false,
							  "error",
							  "Validation failed; operation rolled back",
							  List.of(),
							  stageValidation.report().diagnostics(),
							  List.of(),
							  List.of());
		}

		context.deferredOut().addAll(formatUnsets(context.projectRoot(), planned.unsets()));
		context.validationRoots().add(new BatchValidationRoot(modelPath, true));
		context.out().println(support.opPrefix(operation) + "STAGED: removed " + targetReference);
		return support.result(operation,
						  ExitCodes.OK,
						  !validated,
						  "staged",
						  "Removed " + targetReference,
						  List.copyOf(formattedSources.keySet()),
						  stageValidation.report().diagnostics(),
						  planned.unsets(),
						  planned.rewrites());
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
}
