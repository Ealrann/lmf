package org.logoce.lmf.cli.assign;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.edit.EditJsonReportWriter;
import org.logoce.lmf.cli.edit.EditOptions;
import org.logoce.lmf.cli.edit.EditValidationContext;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.edit.WorkspaceEditPipeline;
import org.logoce.lmf.cli.format.RootReferenceResolver;
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

public final class ClearRunner
{
	public record Options(boolean json)
	{
	}

	public int run(final CliContext context,
				   final String modelSpec,
				   final String objectReference,
				   final String featureName)
	{
		return run(context, modelSpec, objectReference, featureName, new Options(false));
	}

	public int run(final CliContext context,
				   final String modelSpec,
				   final String objectReference,
				   final String featureName,
				   final Options options)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(modelSpec, "modelSpec");
		Objects.requireNonNull(objectReference, "objectReference");
		Objects.requireNonNull(featureName, "featureName");
		Objects.requireNonNull(options, "options");

		final var resolved = ModelSpecResolver.resolve(context, modelSpec, "clear", options.json());
		if (!resolved.ok())
		{
			return resolved.exitCode();
		}
		return clearInModel(resolved.path(), context, modelSpec, objectReference, featureName, options);
	}

	private int clearInModel(final Path modelPath,
							 final CliContext context,
							 final String requestedModel,
							 final String objectReference,
							 final String featureName,
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
										   "clear",
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
		if (documents == null)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "clear", ExitCodes.INVALID, "Cannot load workspace documents for " + displayPath);
			}
			else
			{
				err.println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var targetDoc = documents.targetDocument();
		final var linkRoots = RootReferenceResolver.collectLinkRoots(targetDoc.document().linkTrees());
		if (linkRoots.isEmpty())
		{
			err.println("No link trees available for " + displayPath);
			err.println("No changes written to " + displayPath);
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "clear", ExitCodes.INVALID, "No link trees available for " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var resolved = new RootReferenceResolver().resolve(linkRoots, objectReference);
		if (!(resolved instanceof RootReferenceResolver.Resolution.Found found))
		{
			final var message = ReferenceResolutionMessage.forResolution(objectReference, resolved);
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "clear", ExitCodes.USAGE, message);
			}
			else
			{
				err.println(message);
				err.println("No changes written to " + displayPath);
			}
			return ExitCodes.USAGE;
		}

		final var planner = new FeatureAssignmentPlanner();
		final TextEdits.TextEdit edit;
		try
		{
			edit = planner.planClearList(found.node(), targetDoc.document().source(), featureName);
		}
		catch (RuntimeException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "clear", ExitCodes.INVALID, message);
			}
			else
			{
				err.println(message);
				err.println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		if (edit == null)
		{
			if (options.json())
			{
				final var outcome = org.logoce.lmf.cli.edit.EditOutcome.noChanges();
				writeJsonResult(context,
								requestedModel,
								displayPath,
								objectReference,
								featureName,
								outcome,
								"OK: nothing to clear",
								ExitCodes.OK);
			}
			else
			{
				context.out().println("OK: nothing to clear for " + featureName + " on " + objectReference + " in " + displayPath);
			}
			return ExitCodes.OK;
		}

		final var pipeline = new WorkspaceEditPipeline();
		final var validationContext = new EditValidationContext.Workspace(prepared, projectRoot, documentLoader);
		final var outcome = pipeline.applyEdits(validationContext,
												java.util.Map.of(targetDoc.path(), List.of(edit)),
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
								objectReference,
								featureName,
								outcome,
								"OK: nothing to clear",
								ExitCodes.OK);
			}
			else
			{
				context.out().println("OK: nothing to clear for " + featureName + " on " + objectReference + " in " + displayPath);
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
								objectReference,
								featureName,
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
							objectReference,
							featureName,
							outcome,
							"OK: cleared " + featureName,
							ExitCodes.OK);
		}
		else
		{
			context.out().println("OK: cleared " + featureName + " on " + objectReference + " in " + displayPath);
		}
		return ExitCodes.OK;
	}

	private static void writeJsonResult(final CliContext context,
										final String requestedModel,
										final String displayPath,
										final String objectReference,
										final String featureName,
										final org.logoce.lmf.cli.edit.EditOutcome outcome,
										final String message,
										final int exitCode)
	{
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("clear")
			.name("projectRoot").value(context.projectRoot().toString())
			.name("model").beginObject()
			.name("requested").value(requestedModel)
			.name("path").value(displayPath)
			.endObject()
			.name("object").value(objectReference)
			.name("feature").value(featureName);

		EditJsonReportWriter.writeOutcome(json, context, outcome);
		EditJsonReportWriter.writeDiagnostics(json, outcome.validationReport());

		json.name("message").value(message)
			.name("ok").value(exitCode == ExitCodes.OK)
			.name("exitCode").value(exitCode)
			.endObject()
			.flush();
		context.out().println();
	}
}
