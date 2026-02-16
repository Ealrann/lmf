package org.logoce.lmf.core.loader.api.tooling.workspace;

import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.parsing.ModelHeaderUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Indexes meta-model headers from disk to support workspace-level registry rebuilds
 * without forcing all meta-models to be opened in the editor.
 */
public final class DiskMetaModelHeaderIndex
{
	private final Map<Path, Long> modelFilesByMtime = new HashMap<>();
	private final Map<Path, DiskMetaModelHeader> headersByPath = new HashMap<>();
	private final Map<String, DiskMetaModelHeader> headersByName = new HashMap<>();

	public void refresh(final Path root) throws IOException
	{
		final var normalizedRoot = root.toAbsolutePath().normalize();
		final var present = new HashSet<Path>();
		Files.walkFileTree(normalizedRoot, new SimpleFileVisitor<>()
		{
			@Override
			public java.nio.file.FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
			{
				if (!dir.equals(normalizedRoot))
				{
					final var name = dir.getFileName();
					if (name != null && WorkspaceScanDefaults.isIgnoredDirectoryName(name.toString()))
					{
						return java.nio.file.FileVisitResult.SKIP_SUBTREE;
					}
				}
				return java.nio.file.FileVisitResult.CONTINUE;
			}

			@Override
			public java.nio.file.FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
			{
				if (attrs != null && !attrs.isRegularFile())
				{
					return java.nio.file.FileVisitResult.CONTINUE;
				}
				if (file.getFileName().toString().endsWith(".lm"))
				{
					present.add(file.toAbsolutePath().normalize());
				}
				return java.nio.file.FileVisitResult.CONTINUE;
			}

			@Override
			public java.nio.file.FileVisitResult visitFileFailed(final Path file, final IOException exc)
			{
				return java.nio.file.FileVisitResult.CONTINUE;
			}
		});

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

	public List<File> resolveMetaModelFilesClosure(final Set<String> requiredNames)
	{
		if (requiredNames == null || requiredNames.isEmpty())
		{
			return List.of();
		}

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

	private record DiskMetaModelHeader(Path path, String qualifiedName, List<String> imports)
	{
	}
}
