package org.logoce.lmf.cli.insert;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.edit.EditOptions;
import org.logoce.lmf.cli.edit.EditValidationContext;
import org.logoce.lmf.cli.edit.WorkspaceEditPipeline;
import org.logoce.lmf.cli.format.LmFormatter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.ModelLocator;
import org.logoce.lmf.cli.workspace.ModelResolution;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.cli.workspace.WorkspaceDocumentsLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

public final class InsertRunner
{
	public int run(final CliContext context,
				   final String modelSpec,
				   final String targetReference,
				   final String subtree)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(modelSpec, "modelSpec");
		Objects.requireNonNull(targetReference, "targetReference");
		Objects.requireNonNull(subtree, "subtree");

		final var locator = new ModelLocator(context.projectRoot());
		final var resolution = locator.resolve(modelSpec);

		if (resolution instanceof ModelResolution.Found found)
		{
			return insertIntoModel(found.path(), context, targetReference, subtree);
		}
		if (resolution instanceof ModelResolution.Ambiguous ambiguous)
		{
			final var err = context.err();
			err.println("Ambiguous model reference: " + modelSpec);
			for (final var path : ambiguous.matches())
			{
				err.println(" - " + PathDisplay.display(context.projectRoot(), path));
			}
			return ExitCodes.USAGE;
		}
		if (resolution instanceof ModelResolution.NotFound notFound)
		{
			final var err = context.err();
			err.println("Model not found: " + notFound.requested());
			err.println("Searched under: " + context.projectRoot());
			return ExitCodes.USAGE;
		}
		if (resolution instanceof ModelResolution.Failed failed)
		{
			final var err = context.err();
			err.println("Failed to search for model: " + failed.message());
			return ExitCodes.USAGE;
		}

		context.err().println("Unexpected model resolution state");
		return ExitCodes.USAGE;
	}

	private int insertIntoModel(final Path modelPath,
								final CliContext context,
								final String targetReference,
								final String subtree)
	{
		final var projectRoot = context.projectRoot();
		final var displayPath = PathDisplay.display(projectRoot, modelPath);
		final var err = context.err();

		final var formattedSubtree = formatSingleRootSubtree(subtree, err);
		if (formattedSubtree == null)
		{
			err.println("No changes written to " + displayPath);
			return ExitCodes.USAGE;
		}

		final var documentLoader = new DocumentLoader();
		final var registryService = new RegistryService(projectRoot, documentLoader);
		final var prepareResult = registryService.prepareForModelAndImporters(modelPath, err, true);
		if (prepareResult instanceof RegistryService.PrepareWorkspaceResult.Failure failure)
		{
			err.println("No changes written to " + displayPath);
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
			err.println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		final var planner = new InsertPlanner();
		final var planResult = planner.plan(documents, targetReference, formattedSubtree);
		if (planResult instanceof InsertPlanResult.Failure failure)
		{
			err.println(failure.message());
			err.println("No changes written to " + displayPath);
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
			context.out().println("OK: nothing to insert into " + displayPath);
			return ExitCodes.OK;
		}

		if (!outcome.validationPassed() || !outcome.wrote())
		{
			err.println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		context.out().println("OK: inserted into " + displayPath + " (updated " + outcome.sources().size() + " file(s))");
		return ExitCodes.OK;
	}

	private static String formatSingleRootSubtree(final String subtreeSource, final java.io.PrintWriter err)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var reader = new LmTreeReader();
		final var readResult = reader.read(subtreeSource, diagnostics);

		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			DiagnosticReporter.printDiagnostics(err, "<subtree>", diagnostics);
			err.println("Insertion subtree cannot be parsed");
			return null;
		}

		if (readResult.roots().size() != 1)
		{
			err.println("Insertion subtree must contain exactly one root element; found: " + readResult.roots().size());
			return null;
		}

		return new LmFormatter().format(readResult.roots());
	}
}
