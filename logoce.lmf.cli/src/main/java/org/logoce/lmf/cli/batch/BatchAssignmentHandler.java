package org.logoce.lmf.cli.batch;

import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.assign.FeatureAssignmentPlanner;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.util.List;
import java.util.Map;
import java.util.Objects;

final class BatchAssignmentHandler implements BatchOperationHandler
{
	@FunctionalInterface
	interface Planner
	{
		TextEdits.TextEdit plan(FeatureAssignmentPlanner planner,
							LinkNodeInternal<?, PNode, ?> node,
							CharSequence source,
							String featureName,
							String value);
	}

	record Spec(String command,
				int argsWithModel,
				int argsWithDefaultModel,
				boolean requiresValue,
				String usageTail,
				Planner planner,
				String noopOutput,
				String noopResult,
				String stagedFormat,
				String resultFormat)
	{
		Spec
		{
			Objects.requireNonNull(command, "command");
			Objects.requireNonNull(usageTail, "usageTail");
			Objects.requireNonNull(planner, "planner");
			Objects.requireNonNull(stagedFormat, "stagedFormat");
			Objects.requireNonNull(resultFormat, "resultFormat");
		}
	}

	private final BatchOperationSupport support;
	private final Spec spec;

	BatchAssignmentHandler(final BatchOperationSupport support, final Spec spec)
	{
		this.support = Objects.requireNonNull(support, "support");
		this.spec = Objects.requireNonNull(spec, "spec");
	}

	@Override
	public BatchOperationResult execute(final BatchOperation operation)
	{
		final var context = support.context();
		final var options = context.options();
		final var parsed = support.parseModelArgs(operation,
									 options,
									 spec.argsWithModel(),
									 spec.argsWithDefaultModel());
		if (parsed == null)
		{
			return support.invalidArgs(operation, options, spec.command(), spec.usageTail());
		}

		final var modelSpec = parsed.modelSpec();
		final var objectReference = parsed.args().get(0);
		final var featureName = parsed.args().get(1);
		final var value = spec.requiresValue() ? parsed.args().get(2) : null;

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

		final var targetDoc = workspace.documents().targetDocument();
		final var linkRoots = RootReferenceResolver.collectLinkRoots(targetDoc.document().linkTrees());
		if (linkRoots.isEmpty())
		{
			context.err().println(support.opPrefix(operation) + "No link trees available for " + PathDisplay.display(context.projectRoot(), modelPath));
			return support.result(operation,
							  ExitCodes.INVALID,
							  false,
							  "error",
							  "No link trees available for target document",
							  List.of(),
							  List.of());
		}

		final var resolved = new RootReferenceResolver().resolve(linkRoots, objectReference);
		if (!(resolved instanceof RootReferenceResolver.Resolution.Found found))
		{
			context.err().println(support.opPrefix(operation) + support.referenceResolutionMessage(objectReference, resolved));
			return support.result(operation,
							  ExitCodes.USAGE,
							  false,
							  "error",
							  support.referenceResolutionMessage(objectReference, resolved),
							  List.of(),
							  List.of());
		}

		final TextEdits.TextEdit edit;
		try
		{
			edit = spec.planner().plan(new FeatureAssignmentPlanner(),
									 found.node(),
									 targetDoc.document().source(),
									 featureName,
									 value);
		}
		catch (RuntimeException e)
		{
			context.err().println(support.opPrefix(operation) + support.messageOf(e));
			return support.result(operation,
							  ExitCodes.INVALID,
							  false,
							  "error",
							  support.messageOf(e),
							  List.of(),
							  List.of());
		}

		if (edit == null)
		{
			final var noopOutput = spec.noopOutput() == null ? "nothing to " + spec.command() : spec.noopOutput();
			final var noopResult = spec.noopResult() == null ? "Nothing to " + spec.command() : spec.noopResult();
			context.out().println(support.opPrefix(operation) + "OK: " + noopOutput);
			return support.result(operation,
							  ExitCodes.OK,
							  false,
							  "noop",
							  noopResult,
							  List.of(),
							  List.of());
		}

		final var updatedSources = context.editApplier().apply(Map.of(targetDoc.path(), List.of(edit)),
									  workspace.documents().sourcesByPath());
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
		final var stagedMessage = spec.stagedFormat().formatted(featureName, objectReference);
		context.out().println(support.opPrefix(operation) + "STAGED: " + stagedMessage);
		return support.result(operation,
						  ExitCodes.OK,
						  !validated,
						  "staged",
						  spec.resultFormat().formatted(featureName, objectReference),
						  List.copyOf(formattedSources.keySet()),
						  stageValidation.report().diagnostics(),
						  List.of(),
						  List.of());
	}

	static Spec setSpec()
	{
		return new Spec("set",
						4,
						3,
						true,
						"<model.lm> <objectRef> <featureName> <value>",
						(planner, node, source, feature, value) -> planner.planSet(node, source, feature, value),
						"nothing to set",
						"Nothing to set",
						"set %s on %s",
						"Set %s on %s");
	}

	static Spec unsetSpec()
	{
		return new Spec("unset",
						3,
						2,
						false,
						"<model.lm> <objectRef> <featureName>",
						(planner, node, source, feature, value) -> planner.planUnset(node, source, feature),
						"nothing to unset",
						"Nothing to unset",
						"unset %s on %s",
						"Unset %s on %s");
	}

	static Spec addSpec()
	{
		return new Spec("add",
						4,
						3,
						true,
						"<model.lm> <objectRef> <featureName> <value>",
						(planner, node, source, feature, value) -> planner.planAdd(node, source, feature, value),
						"nothing to add",
						"Nothing to add",
						"added to %s on %s",
						"Added to %s on %s");
	}

	static Spec removeValueSpec()
	{
		return new Spec("remove-value",
						4,
						3,
						true,
						"<model.lm> <objectRef> <featureName> <value>",
						(planner, node, source, feature, value) -> planner.planRemoveValue(node, source, feature, value),
						"nothing to remove",
						"Nothing to remove",
						"removed value from %s on %s",
						"Removed value from %s on %s");
	}

	static Spec clearSpec()
	{
		return new Spec("clear",
						3,
						2,
						false,
						"<model.lm> <objectRef> <featureName>",
						(planner, node, source, feature, value) -> planner.planClearList(node, source, feature),
						"nothing to clear",
						"Nothing to clear",
						"cleared %s on %s",
						"Cleared %s on %s");
	}
}
