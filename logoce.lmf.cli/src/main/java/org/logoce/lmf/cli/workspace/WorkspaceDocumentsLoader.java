package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.lang.Model;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorkspaceDocumentsLoader
{
	public WorkspaceDocuments load(final DocumentLoader documentLoader,
								   final ModelRegistry registry,
								   final RegistryService.PreparedWorkspace prepared,
								   final Path projectRoot,
								   final PrintWriter err,
								   final String displayTargetPath)
	{
		final var documents = new ArrayList<WorkspaceModelDocument>();
		final var sourcesByPath = new HashMap<Path, String>();
		WorkspaceModelDocument targetDocument = null;

		for (final var entry : prepared.scanModelPaths().entrySet())
		{
			final var qualifiedName = entry.getKey();
			final var path = entry.getValue();
			final var displayPath = PathDisplay.display(projectRoot, path);

			final var doc = documentLoader.loadModelFromPath(registry, qualifiedName, path, err);
			if (doc == null)
			{
				err.println("Cannot load model: " + displayPath);
				return null;
			}

			if (DiagnosticReporter.hasErrors(doc.diagnostics()))
			{
				DiagnosticReporter.printDiagnostics(err, displayPath, doc.diagnostics());
				return null;
			}

			if (!(doc.model() instanceof Model))
			{
				err.println("Input doesn't define a valid model: " + displayPath);
				return null;
			}

			sourcesByPath.put(path, doc.source().toString());
			final var modelDoc = new WorkspaceModelDocument(qualifiedName, path, doc);
			documents.add(modelDoc);

			if (qualifiedName.equals(prepared.targetQualifiedName()))
			{
				targetDocument = modelDoc;
			}
		}

		if (targetDocument == null)
		{
			err.println("Cannot resolve target model document for " + displayTargetPath);
			return null;
		}

		return new WorkspaceDocuments(targetDocument,
									  List.copyOf(documents),
									  Map.copyOf(sourcesByPath));
	}
}
