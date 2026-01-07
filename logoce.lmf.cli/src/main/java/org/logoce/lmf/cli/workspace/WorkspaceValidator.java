package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.lang.Model;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

public final class WorkspaceValidator
{
	public boolean validate(final RegistryService.PreparedWorkspace prepared,
							final Path projectRoot,
							final Map<Path, String> updatedSources,
							final DocumentLoader documentLoader,
							final PrintWriter err)
	{
		final var registryBuilder = new ModelRegistry.Builder(prepared.registry());
		for (final var name : prepared.registryModelPaths().keySet())
		{
			registryBuilder.remove(name);
		}

		final var toLoad = new LinkedHashSet<String>();
		for (final var entry : prepared.registryModelPaths().entrySet())
		{
			final var name = entry.getKey();
			final var header = prepared.headersByQualifiedName().get(name);
			if (header == null || header.metaModelRoot())
			{
				continue;
			}
			toLoad.add(name);
		}

		final var remaining = new LinkedHashSet<String>(toLoad);
		final var loaded = new HashSet<String>();

		while (!remaining.isEmpty())
		{
			String next = null;
			for (final var candidate : remaining)
			{
				final var header = prepared.headersByQualifiedName().get(candidate);
				if (header == null)
				{
					continue;
				}

				final boolean depsLoaded = header.imports()
											  .stream()
											  .filter(remaining::contains)
											  .allMatch(loaded::contains);
				if (depsLoaded)
				{
					next = candidate;
					break;
				}
			}

			if (next == null)
			{
				next = remaining.iterator().next();
			}

			remaining.remove(next);
			final var path = prepared.registryModelPaths().get(next);
			final var source = resolveSource(updatedSources, documentLoader, path, err);
			if (source == null)
			{
				return false;
			}

			final var doc = documentLoader.loadModelFromSource(registryBuilder.build(), next, source, err);
			if (doc == null)
			{
				return false;
			}
			if (DiagnosticReporter.hasErrors(doc.diagnostics()))
			{
				final var displayPath = PathDisplay.display(projectRoot, path);
				DiagnosticReporter.printDiagnostics(err, displayPath, doc.diagnostics());
				return false;
			}
			if (doc.model() instanceof Model model)
			{
				registryBuilder.register(model);
			}
			else
			{
				final var displayPath = PathDisplay.display(projectRoot, path);
				err.println("Input doesn't define a valid model: " + displayPath);
				return false;
			}

			loaded.add(next);
		}

		return true;
	}

	private static String resolveSource(final Map<Path, String> updatedSources,
										final DocumentLoader documentLoader,
										final Path path,
										final PrintWriter err)
	{
		final var updated = updatedSources.get(path);
		if (updated != null)
		{
			return updated;
		}
		return documentLoader.readString(path, err);
	}
}

