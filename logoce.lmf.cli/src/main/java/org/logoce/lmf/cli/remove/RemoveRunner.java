package org.logoce.lmf.cli.remove;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.edit.EditJsonReportWriter;
import org.logoce.lmf.cli.edit.EditOptions;
import org.logoce.lmf.cli.edit.EditValidationContext;
import org.logoce.lmf.cli.edit.WorkspaceEditPipeline;
import org.logoce.lmf.cli.json.JsonErrorWriter;
import org.logoce.lmf.cli.json.JsonWriter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.ModelSpecResolver;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.cli.workspace.WorkspaceDocumentsLoader;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class RemoveRunner
{
	public record Options(boolean json)
	{
	}

	public int run(final CliContext context,
				   final String modelSpec,
				   final String targetReference)
	{
		return run(context, modelSpec, targetReference, new Options(false));
	}

	public int run(final CliContext context,
				   final String modelSpec,
				   final String targetReference,
				   final Options options)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(modelSpec, "modelSpec");
		Objects.requireNonNull(targetReference, "targetReference");
		Objects.requireNonNull(options, "options");

		final var resolved = ModelSpecResolver.resolve(context, modelSpec, "remove", options.json());
		if (!resolved.ok())
		{
			return resolved.exitCode();
		}
		return removeFromModel(resolved.path(), context, modelSpec, targetReference, options);
	}

	private int removeFromModel(final Path modelPath,
								final CliContext context,
								final String modelSpec,
								final String targetReference,
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
										   "remove",
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
				JsonErrorWriter.writeError(context, "remove", ExitCodes.INVALID, "Cannot load workspace documents for " + displayPath);
			}
			else
			{
				err.println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var removeWorkspace = toRemoveWorkspace(documents);
		if (removeWorkspace == null)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "remove", ExitCodes.INVALID, "Cannot build remove workspace for " + displayPath);
			}
			else
			{
				err.println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var planner = new RemovePlanner();
		final var planResult = planner.plan(removeWorkspace, targetReference);
		if (planResult instanceof RemovePlanResult.Failure failure)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "remove", ExitCodes.INVALID, failure.message());
			}
			else
			{
				err.println(failure.message());
				err.println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var planned = ((RemovePlanResult.Success) planResult).edit();
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
								modelSpec,
								displayPath,
								targetReference,
								planned,
								outcome,
								"OK: nothing to remove",
								ExitCodes.OK);
			}
			else
			{
				context.out().println("OK: nothing to remove in " + displayPath);
			}
			return ExitCodes.OK;
		}

		if (!outcome.validationPassed() || !outcome.wrote())
		{
			if (options.json())
			{
				writeJsonResult(context,
								modelSpec,
								displayPath,
								targetReference,
								planned,
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
							modelSpec,
							displayPath,
							targetReference,
							planned,
							outcome,
							"OK: removed " + targetReference,
							ExitCodes.OK);
		}
		else
		{
			printUnsets(context, planned.unsets());
			context.out().println("OK: removed " + targetReference + " from " + displayPath);
		}
		return ExitCodes.OK;
	}

	private static RemoveWorkspace toRemoveWorkspace(final org.logoce.lmf.cli.workspace.WorkspaceDocuments workspace)
	{
		final var targetPath = workspace.targetDocument().path();
		final var documents = new java.util.ArrayList<RemoveModelDocument>();
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

	private static void printUnsets(final CliContext context, final List<RemoveUnsetReference> unsets)
	{
		if (unsets.isEmpty())
		{
			return;
		}

		for (final var unset : unsets)
		{
			final var location = formatLocation(context.projectRoot(), unset.path(), unset.span());
			final var resolved = unset.targetId().modelQualifiedName() + unset.targetId().path();
			context.out().println(location + "\t" + unset.raw() + "\t" + resolved + "\tunset");
		}
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

	private static void writeJsonResult(final CliContext context,
										final String requestedModel,
										final String displayPath,
										final String targetReference,
										final RemovePlannedEdit planned,
										final org.logoce.lmf.cli.edit.EditOutcome outcome,
										final String message,
										final int exitCode)
	{
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("remove")
			.name("projectRoot").value(context.projectRoot().toString())
			.name("model").beginObject()
			.name("requested").value(requestedModel)
			.name("path").value(displayPath)
			.endObject()
			.name("target").beginObject()
			.name("reference").value(targetReference)
			.name("removed").beginObject()
			.name("modelQualifiedName").value(planned.removedId().modelQualifiedName())
			.name("path").value(planned.removedId().path())
			.endObject()
			.endObject();

		EditJsonReportWriter.writeUnsets(json, context, planned.unsets());
		EditJsonReportWriter.writeReferenceRewrites(json, context, planned.rewrites());
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
