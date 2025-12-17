package org.logoce.lmf.lsp.workspace;

import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.parsing.ModelHeaderUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Indexes meta-model headers from disk to support workspace-level registry rebuilds
 * without forcing all meta-models to be opened in the editor.
 */
final class DiskMetaModelHeaderIndex
{
	private final Map<Path, Long> modelFilesByMtime = new HashMap<>();
	private final Map<Path, DiskMetaModelHeader> headersByPath = new HashMap<>();
	private final Map<String, DiskMetaModelHeader> headersByName = new HashMap<>();

	void refresh(final Path root) throws IOException
	{
		final var present = new HashSet<Path>();
		try (final var paths = Files.walk(root))
		{
			paths.filter(Files::isRegularFile)
				 .filter(p -> p.getFileName().toString().endsWith(".lm"))
				 .forEach(present::add);
		}

		final var removed = new ArrayList<Path>();
		for (final var path : modelFilesByMtime.keySet())
		{
			if (!present.contains(path))
			{
				removed.add(path);
			}
		}
		for (final var path : removed)
		{
			modelFilesByMtime.remove(path);
			headersByPath.remove(path);
		}

		final var reader = new LmTreeReader();
		for (final var path : present)
		{
			final long mtime;
			try
			{
				mtime = Files.getLastModifiedTime(path).toMillis();
			}
			catch (Exception e)
			{
				continue;
			}

			final var previous = modelFilesByMtime.get(path);
			if (previous != null && previous.longValue() == mtime)
			{
				continue;
			}

			try
			{
				final var text = Files.readString(path);
				final var diagnostics = new ArrayList<LmDiagnostic>();
				final var readResult = reader.read(text, diagnostics);
				final var roots = readResult.roots();

				if (roots.isEmpty() || !ModelHeaderUtil.isMetaModelRoot(roots))
				{
					headersByPath.remove(path);
				}
				else
				{
					final var rootNode = roots.getFirst().data();
					final var qualifiedName = qualifiedNameFromHeader(rootNode);
					if (qualifiedName == null)
					{
						headersByPath.remove(path);
					}
					else
					{
						final var imports = ModelHeaderUtil.resolveImports(rootNode);
						headersByPath.put(path, new DiskMetaModelHeader(path, qualifiedName, List.copyOf(imports)));
					}
				}
			}
			catch (Exception e)
			{
				headersByPath.remove(path);
			}
			finally
			{
				modelFilesByMtime.put(path, mtime);
			}
		}

		headersByName.clear();
		for (final var header : headersByPath.values())
		{
			headersByName.put(header.qualifiedName(), header);
		}
	}

	List<File> resolveMetaModelFilesClosure(final HashSet<String> requiredNames)
	{
		final var closure = new HashSet<String>(requiredNames);
		final var queue = new ArrayList<String>(requiredNames);

		while (!queue.isEmpty())
		{
			final String name = queue.removeLast();
			final var header = headersByName.get(name);
			if (header == null)
			{
				continue;
			}

			for (final var imp : header.imports())
			{
				if (closure.add(imp))
				{
					queue.add(imp);
				}
			}
		}

		final var files = new ArrayList<File>();
		for (final var name : closure)
		{
			final var header = headersByName.get(name);
			if (header == null)
			{
				continue;
			}
			files.add(header.path().toFile());
		}

		return List.copyOf(files);
	}

	private static String qualifiedNameFromHeader(final org.logoce.lmf.core.loader.api.text.syntax.PNode rootNode)
	{
		final String domain = ModelHeaderUtil.resolveDomain(rootNode);
		final String name = ModelHeaderUtil.resolveName(rootNode);
		if (name == null || name.isBlank())
		{
			return null;
		}
		if (domain == null || domain.isBlank())
		{
			return name;
		}
		return domain + "." + name;
	}

	record DiskMetaModelHeader(Path path, String qualifiedName, List<String> imports)
	{
	}
}

