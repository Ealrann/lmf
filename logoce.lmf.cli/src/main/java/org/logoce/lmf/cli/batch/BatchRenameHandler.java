package org.logoce.lmf.cli.batch;

import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.rename.RenamePlanResult;
import org.logoce.lmf.cli.rename.RenamePlanner;

import java.util.List;
import java.util.Objects;

final class BatchRenameHandler implements BatchOperationHandler
{
	private final BatchOperationSupport support;

	BatchRenameHandler(final BatchOperationSupport support)
	{
		this.support = Objects.requireNonNull(support, "support");
	}

	@Override
	public BatchOperationResult execute(final BatchOperation operation)
	{
		final var context = support.context();
		final var options = context.options();
		final var parsed = support.parseModelArgs(operation, options, 3, 2);
		if (parsed == null)
		{
			return support.invalidArgs(operation, options, "rename", "<model.lm> <ref> <newName>");
		}

		final var modelSpec = parsed.modelSpec();
		final var targetReference = parsed.args().get(0);
		final var newName = parsed.args().get(1);

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

		final var planner = new RenamePlanner();
		final var planResult = planner.plan(workspace.documents(), targetReference, newName);
		if (planResult instanceof RenamePlanResult.Failure failure)
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

		final var planned = ((RenamePlanResult.Success) planResult).edit();
		if (!planned.changed() || planned.editsByFile().isEmpty())
		{
			context.out().println(support.opPrefix(operation) + "OK: nothing to rename");
			return support.result(operation,
							  ExitCodes.OK,
							  false,
							  "noop",
							  "Nothing to rename",
							  List.of(),
							  List.of());
		}

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

		context.validationRoots().add(new BatchValidationRoot(modelPath, true));
		context.out().println(support.opPrefix(operation) + "STAGED: renamed " + targetReference);
		return support.result(operation,
						  ExitCodes.OK,
						  !validated,
						  "staged",
						  "Renamed " + targetReference,
						  List.copyOf(formattedSources.keySet()),
						  stageValidation.report().diagnostics(),
						  List.of(),
						  List.of());
	}
}
