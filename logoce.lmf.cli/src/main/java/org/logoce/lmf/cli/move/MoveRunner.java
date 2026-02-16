package org.logoce.lmf.cli.move;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.edit.EditJsonReportWriter;
import org.logoce.lmf.cli.edit.EditOptions;
import org.logoce.lmf.cli.edit.EditValidationContext;
import org.logoce.lmf.cli.edit.ReferenceRewrite;
import org.logoce.lmf.cli.edit.WorkspaceEditPipeline;
import org.logoce.lmf.cli.json.JsonErrorWriter;
import org.logoce.lmf.cli.json.JsonWriter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.ModelSpecResolver;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.cli.workspace.WorkspaceDocumentsLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class MoveRunner
{
	public record Options(boolean json)
	{
	}

	public int run(final CliContext context,
				   final String modelSpec,
				   final String fromReference,
				   final String toReference)
	{
		return run(context, modelSpec, fromReference, toReference, new Options(false));
	}

	public int run(final CliContext context,
				   final String modelSpec,
				   final String fromReference,
				   final String toReference,
				   final Options options)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(modelSpec, "modelSpec");
		Objects.requireNonNull(fromReference, "fromReference");
		Objects.requireNonNull(toReference, "toReference");
		Objects.requireNonNull(options, "options");

		final var resolved = ModelSpecResolver.resolve(context, modelSpec, "move", options.json());
		if (!resolved.ok())
		{
			return resolved.exitCode();
		}
		return moveInModel(resolved.path(), context, modelSpec, fromReference, toReference, options);
	}

	private int moveInModel(final Path modelPath,
							final CliContext context,
							final String requestedModel,
							final String fromReference,
							final String toReference,
							final Options options)
	{
		final var projectRoot = context.projectRoot();
		final var displayPath = PathDisplay.display(projectRoot, modelPath);
		final var err = context.err();

		final var documentLoader = new DocumentLoader();
		final var registryService = new RegistryService(projectRoot, documentLoader);
		final var prepareResult = registryService.prepareForModelAndImporters(modelPath, err, true);
		if (prepareResult instanceof RegistryService.PrepareWorkspaceResult.Failure failure)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context,
										   "move",
										   failure.exitCode(),
										   "Cannot prepare workspace for " + displayPath);
			}
			else
			{
				err.println("No changes written to " + displayPath);
			}
			return failure.exitCode();
		}

		final var prepared = ((RegistryService.PrepareWorkspaceResult.Success) prepareResult).workspace();
		final var registry = prepared.registry();

		final var documentsLoader = new WorkspaceDocumentsLoader();
		final var documents = documentsLoader.load(documentLoader,
								   registry,
								   prepared,
								   projectRoot,
								   err,
								   displayPath);
		if (documents == null || documents.targetDocument() == null)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "move", ExitCodes.INVALID, "Cannot load workspace documents for " + displayPath);
			}
			else
			{
				err.println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var planner = new MovePlanner();
		final var planResult = planner.plan(documents, fromReference, toReference);
		if (planResult instanceof MovePlanResult.Failure failure)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "move", ExitCodes.INVALID, failure.message());
			}
			else
			{
				err.println(failure.message());
				err.println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var planned = ((MovePlanResult.Success) planResult).edit();
		if (!planned.changed() || planned.editsByFile().isEmpty())
		{
			if (options.json())
			{
				final var outcome = org.logoce.lmf.cli.edit.EditOutcome.noChanges();
				writeJsonResult(context,
								requestedModel,
								displayPath,
								fromReference,
								toReference,
								planned.rewrites(),
								outcome,
								"OK: nothing to move",
								ExitCodes.OK);
			}
			else
			{
				context.out().println("OK: nothing to move in " + displayPath);
			}
			return ExitCodes.OK;
		}

		final var pipeline = new WorkspaceEditPipeline();
		final var validationContext = new EditValidationContext.Workspace(prepared, projectRoot, documentLoader);
		final var outcome = pipeline.applyEdits(validationContext,
												planned.editsByFile(),
												documents.sourcesByPath(),
												new EditOptions(true, true, false, true),
												err);

		if (!outcome.changed())
		{
			if (options.json())
			{
				writeJsonResult(context,
								requestedModel,
								displayPath,
								fromReference,
								toReference,
								planned.rewrites(),
								outcome,
								"OK: nothing to move",
								ExitCodes.OK);
			}
			else
			{
				context.out().println("OK: nothing to move in " + displayPath);
			}
			return ExitCodes.OK;
		}

		if (!outcome.validationPassed() || !outcome.wrote())
		{
			if (options.json())
			{
				writeJsonResult(context,
								requestedModel,
								displayPath,
								fromReference,
								toReference,
								planned.rewrites(),
								outcome,
								"No changes written",
								ExitCodes.INVALID);
			}
			else
			{
				err.println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		if (options.json())
		{
			writeJsonResult(context,
							requestedModel,
							displayPath,
							fromReference,
							toReference,
							planned.rewrites(),
							outcome,
							"OK: moved " + fromReference + " to " + toReference,
							ExitCodes.OK);
		}
		else
		{
			context.out().println("OK: moved " + fromReference + " to " + toReference + " in " + displayPath);
		}
		return ExitCodes.OK;
	}

	private static void writeJsonResult(final CliContext context,
										final String requestedModel,
										final String displayPath,
										final String fromReference,
										final String toReference,
										final List<ReferenceRewrite> rewrites,
										final org.logoce.lmf.cli.edit.EditOutcome outcome,
										final String message,
										final int exitCode)
	{
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("move")
			.name("projectRoot").value(context.projectRoot().toString())
			.name("model").beginObject()
			.name("requested").value(requestedModel)
			.name("path").value(displayPath)
			.endObject()
			.name("from").value(fromReference)
			.name("to").value(toReference);

		EditJsonReportWriter.writeOutcome(json, context, outcome);
		EditJsonReportWriter.writeReferenceRewrites(json, context, rewrites);
		EditJsonReportWriter.writeDiagnostics(json, outcome.validationReport());

		json.name("message").value(message)
			.name("ok").value(exitCode == ExitCodes.OK)
			.name("exitCode").value(exitCode)
			.endObject()
			.flush();
		context.out().println();
	}
}
