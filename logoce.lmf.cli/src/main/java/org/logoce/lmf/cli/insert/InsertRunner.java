package org.logoce.lmf.cli.insert;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticItem;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.diagnostics.ValidationReport;
import org.logoce.lmf.cli.edit.EditJsonReportWriter;
import org.logoce.lmf.cli.edit.EditOptions;
import org.logoce.lmf.cli.edit.EditValidationContext;
import org.logoce.lmf.cli.edit.WorkspaceEditPipeline;
import org.logoce.lmf.cli.format.LmFormatter;
import org.logoce.lmf.cli.json.JsonErrorWriter;
import org.logoce.lmf.cli.json.JsonWriter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.ModelSpecResolver;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.cli.workspace.WorkspaceDocumentsLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class InsertRunner
{
	public record Options(boolean json)
	{
	}

	public int run(final CliContext context,
				   final String modelSpec,
				   final String targetReference,
				   final String subtree)
	{
		return run(context, modelSpec, targetReference, subtree, new Options(false));
	}

	public int run(final CliContext context,
				   final String modelSpec,
				   final String targetReference,
				   final String subtree,
				   final Options options)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(modelSpec, "modelSpec");
		Objects.requireNonNull(targetReference, "targetReference");
		Objects.requireNonNull(subtree, "subtree");
		Objects.requireNonNull(options, "options");

		final var resolved = ModelSpecResolver.resolve(context, modelSpec, "insert", options.json());
		if (!resolved.ok())
		{
			return resolved.exitCode();
		}
		return insertIntoModel(resolved.path(), context, modelSpec, targetReference, subtree, options);
	}

	private int insertIntoModel(final Path modelPath,
								final CliContext context,
								final String modelSpec,
								final String targetReference,
								final String subtree,
								final Options options)
	{
		final var projectRoot = context.projectRoot();
		final var displayPath = PathDisplay.display(projectRoot, modelPath);
		final var err = context.err();

		final var formattedSubtreeResult = formatSingleRootSubtree(subtree, err);
		if (formattedSubtreeResult instanceof SubtreeFormatResult.Failure failure)
		{
			final var report = new ValidationReport(false, toSubtreeDiagnostics(failure.diagnostics()), List.of());
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "insert", ExitCodes.USAGE, failure.message(), report);
			}
			else
			{
				err.println("No changes written to " + displayPath);
			}
			return ExitCodes.USAGE;
		}
		final var formattedSubtree = ((SubtreeFormatResult.Success) formattedSubtreeResult).formatted();

		final var documentLoader = new DocumentLoader();
		final var registryService = new RegistryService(projectRoot, documentLoader);
		final var prepareResult = registryService.prepareForModelAndImporters(modelPath, err, true);
		if (prepareResult instanceof RegistryService.PrepareWorkspaceResult.Failure failure)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context,
										   "insert",
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
				JsonErrorWriter.writeError(context, "insert", ExitCodes.INVALID, "Cannot load workspace documents for " + displayPath);
			}
			else
			{
				err.println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var planner = new InsertPlanner();
		final var planResult = planner.plan(documents, targetReference, formattedSubtree);
		if (planResult instanceof InsertPlanResult.Failure failure)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "insert", ExitCodes.INVALID, failure.message());
			}
			else
			{
				err.println(failure.message());
				err.println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var planned = ((InsertPlanResult.Success) planResult).edit();
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
								"OK: nothing to insert",
								ExitCodes.OK);
			}
			else
			{
				context.out().println("OK: nothing to insert into " + displayPath);
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
							"OK: inserted into " + targetReference,
							ExitCodes.OK);
		}
		else
		{
			context.out().println("OK: inserted into " + displayPath + " (updated " + outcome.sources().size() + " file(s))");
		}
		return ExitCodes.OK;
	}

	private sealed interface SubtreeFormatResult permits SubtreeFormatResult.Success, SubtreeFormatResult.Failure
	{
		record Success(String formatted) implements SubtreeFormatResult
		{
		}

			record Failure(String message, List<LmDiagnostic> diagnostics) implements SubtreeFormatResult
			{
				public Failure
				{
					Objects.requireNonNull(message, "message");
					diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
				}
			}
	}

	private static List<DiagnosticItem> toSubtreeDiagnostics(final List<LmDiagnostic> diagnostics)
	{
		if (diagnostics == null || diagnostics.isEmpty())
		{
			return List.of();
		}
		return diagnostics.stream()
						  .map(diagnostic -> new DiagnosticItem("<subtree>", diagnostic))
						  .toList();
	}

	private static SubtreeFormatResult formatSingleRootSubtree(final String subtreeSource, final java.io.PrintWriter err)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var reader = new LmTreeReader();
		final var readResult = reader.read(subtreeSource, diagnostics);

		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			DiagnosticReporter.printDiagnostics(err, "<subtree>", diagnostics);
			final var message = "Insertion subtree cannot be parsed";
			err.println(message);
			err.println("Hint: If you passed a subtree inline, prefer --subtree-stdin/--subtree-file to avoid shell quoting issues.");
			return new SubtreeFormatResult.Failure(message, List.copyOf(diagnostics));
		}

		if (readResult.roots().size() != 1)
		{
			final var message = "Insertion subtree must contain exactly one root element; found: " + readResult.roots().size();
			err.println(message);
			return new SubtreeFormatResult.Failure(message, List.of());
		}

		return new SubtreeFormatResult.Success(new LmFormatter().format(readResult.roots()));
	}

	private static void writeJsonResult(final CliContext context,
										final String requestedModel,
										final String displayPath,
										final String targetReference,
										final InsertPlannedEdit planned,
										final org.logoce.lmf.cli.edit.EditOutcome outcome,
										final String message,
										final int exitCode)
	{
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("insert")
			.name("projectRoot").value(context.projectRoot().toString())
			.name("model").beginObject()
			.name("requested").value(requestedModel)
			.name("path").value(displayPath)
			.endObject()
			.name("target").beginObject()
			.name("reference").value(targetReference)
			.endObject();

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
