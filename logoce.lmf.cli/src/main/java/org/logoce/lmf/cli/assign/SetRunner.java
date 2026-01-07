package org.logoce.lmf.cli.assign;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.edit.EditOptions;
import org.logoce.lmf.cli.edit.EditValidationContext;
import org.logoce.lmf.cli.edit.WorkspaceEditPipeline;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.ModelLocator;
import org.logoce.lmf.cli.workspace.ModelResolution;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.cli.workspace.WorkspaceDocumentsLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class SetRunner
{
	public int run(final CliContext context,
				   final String modelSpec,
				   final String objectReference,
				   final String featureName,
				   final String value)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(modelSpec, "modelSpec");
		Objects.requireNonNull(objectReference, "objectReference");
		Objects.requireNonNull(featureName, "featureName");
		Objects.requireNonNull(value, "value");

		final var locator = new ModelLocator(context.projectRoot());
		final var resolution = locator.resolve(modelSpec);

		if (resolution instanceof ModelResolution.Found found)
		{
			return setInModel(found.path(), context, objectReference, featureName, value);
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

	private int setInModel(final Path modelPath,
						   final CliContext context,
						   final String objectReference,
						   final String featureName,
						   final String value)
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
		if (documents == null)
		{
			err.println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		final var targetDoc = documents.targetDocument();
		final var linkRoots = RootReferenceResolver.collectLinkRoots(targetDoc.document().linkTrees());
		if (linkRoots.isEmpty())
		{
			err.println("No link trees available for " + displayPath);
			err.println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		final var resolved = new RootReferenceResolver().resolve(linkRoots, objectReference);
		if (!(resolved instanceof RootReferenceResolver.Resolution.Found found))
		{
			err.println(ReferenceResolutionMessage.forResolution(objectReference, resolved));
			err.println("No changes written to " + displayPath);
			return ExitCodes.USAGE;
		}

		final var planner = new FeatureAssignmentPlanner();
		final TextEdits.TextEdit edit;
		try
		{
			edit = planner.planSet(found.node(), targetDoc.document().source(), featureName, value);
		}
		catch (RuntimeException e)
		{
			err.println(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
			err.println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
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
			context.out().println("OK: nothing to set for " + featureName + " on " + objectReference + " in " + displayPath);
			return ExitCodes.OK;
		}

		if (!outcome.validationPassed() || !outcome.wrote())
		{
			err.println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		context.out().println("OK: set " + featureName + " on " + objectReference + " in " + displayPath);
		return ExitCodes.OK;
	}
}
