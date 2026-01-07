package org.logoce.lmf.cli.remove;

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
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class RemoveRunner
{
	public int run(final CliContext context,
				   final String modelSpec,
				   final String targetReference)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(modelSpec, "modelSpec");
		Objects.requireNonNull(targetReference, "targetReference");

		final var locator = new ModelLocator(context.projectRoot());
		final var resolution = locator.resolve(modelSpec);

		if (resolution instanceof ModelResolution.Found found)
		{
			return removeFromModel(found.path(), context, targetReference);
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

	private int removeFromModel(final Path modelPath,
								final CliContext context,
								final String targetReference)
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

		final var removeWorkspace = toRemoveWorkspace(documents);
		if (removeWorkspace == null)
		{
			err.println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		final var planner = new RemovePlanner();
		final var planResult = planner.plan(removeWorkspace, targetReference);
		if (planResult instanceof RemovePlanResult.Failure failure)
		{
			err.println(failure.message());
			err.println("No changes written to " + displayPath);
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
			context.out().println("OK: nothing to remove in " + displayPath);
			return ExitCodes.OK;
		}

		if (!outcome.validationPassed() || !outcome.wrote())
		{
			err.println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		printUnsets(context, planned.unsets());
		context.out().println("OK: removed " + targetReference + " from " + displayPath);
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
}
