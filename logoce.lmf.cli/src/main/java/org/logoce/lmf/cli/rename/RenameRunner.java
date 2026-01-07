package org.logoce.lmf.cli.rename;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.edit.EditOptions;
import org.logoce.lmf.cli.edit.EditValidationContext;
import org.logoce.lmf.cli.edit.WorkspaceEditPipeline;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.ModelLocator;
import org.logoce.lmf.cli.workspace.ModelResolution;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.cli.workspace.WorkspaceDocumentsLoader;

import java.nio.file.Path;
import java.util.Objects;

public final class RenameRunner
{
	public int run(final CliContext context,
				   final String modelSpec,
				   final String targetReference,
				   final String newName)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(modelSpec, "modelSpec");
		Objects.requireNonNull(targetReference, "targetReference");
		Objects.requireNonNull(newName, "newName");

		final var locator = new ModelLocator(context.projectRoot());
		final var resolution = locator.resolve(modelSpec);

		if (resolution instanceof ModelResolution.Found found)
		{
			return renameInModel(found.path(), context, targetReference, newName);
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

	private int renameInModel(final Path modelPath,
						final CliContext context,
						final String targetReference,
						final String newName)
	{
		final var projectRoot = context.projectRoot();
		final var displayPath = PathDisplay.display(projectRoot, modelPath);
		final var err = context.err();

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
		if (documents == null || documents.targetDocument() == null)
		{
			err.println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		final var planner = new RenamePlanner();
		final var planResult = planner.plan(documents, targetReference, newName);
		if (planResult instanceof RenamePlanResult.Failure failure)
		{
			err.println(failure.message());
			err.println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		final var planned = ((RenamePlanResult.Success) planResult).edit();
		if (!planned.changed() || planned.editsByFile().isEmpty())
		{
			context.out().println("OK: nothing to rename in " + displayPath);
			return ExitCodes.OK;
		}

		final var pipeline = new WorkspaceEditPipeline();
		final var validationContext = new EditValidationContext.Workspace(prepared, projectRoot, documentLoader);
		final var outcome = pipeline.applyEdits(validationContext,
												planned.editsByFile(),
												documents.sourcesByPath(),
												new EditOptions(true, true, false, true),
												err);

		if (!outcome.validationPassed() || !outcome.wrote())
		{
			err.println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		context.out().println("OK: renamed " + targetReference + " in " + displayPath);
		return ExitCodes.OK;
	}
}
