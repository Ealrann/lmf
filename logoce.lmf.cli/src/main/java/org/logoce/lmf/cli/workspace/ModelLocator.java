package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.core.loader.api.tooling.workspace.DiskModelHeaderIndex;
import org.logoce.lmf.core.loader.api.tooling.workspace.WorkspaceScanDefaults;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ModelLocator
{
	private static final String QUALIFIED_NAME_PREFIX = "qn:";

	private final Path projectRoot;
	private final DiskModelHeaderIndex headerIndex = new DiskModelHeaderIndex();

	public ModelLocator(final Path projectRoot)
	{
		this.projectRoot = Objects.requireNonNull(projectRoot, "projectRoot")
								  .toAbsolutePath()
								  .normalize();
	}

	public ModelResolution resolve(final String requested)
	{
		if (requested == null || requested.isBlank())
		{
			return new ModelResolution.NotFound("");
		}

		if (requested.startsWith(QUALIFIED_NAME_PREFIX))
		{
			final var qualifiedName = requested.substring(QUALIFIED_NAME_PREFIX.length()).strip();
			if (qualifiedName.isEmpty())
			{
				return new ModelResolution.NotFound(requested);
			}
			return resolveQualifiedName(requested, qualifiedName);
		}

		final var requestedPath = Path.of(requested);
		final var candidate = requestedPath.isAbsolute()
							  ? requestedPath
							  : projectRoot.resolve(requestedPath);
		if (Files.isRegularFile(candidate))
		{
			return new ModelResolution.Found(candidate.toAbsolutePath().normalize());
		}

		final var fileName = requestedPath.getFileName();
		if (fileName == null)
		{
			return new ModelResolution.NotFound(requested);
		}

		try
		{
			final var matches = new java.util.ArrayList<Path>();
			java.nio.file.Files.walkFileTree(projectRoot, new SimpleFileVisitor<>()
			{
				@Override
				public java.nio.file.FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
				{
					if (!dir.equals(projectRoot))
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
					final var currentName = file.getFileName();
					if (currentName != null && currentName.toString().equals(fileName.toString()))
					{
						matches.add(file.toAbsolutePath().normalize());
					}
					return java.nio.file.FileVisitResult.CONTINUE;
				}

				@Override
				public java.nio.file.FileVisitResult visitFileFailed(final Path file, final IOException exc)
				{
					return java.nio.file.FileVisitResult.CONTINUE;
				}
			});

			matches.sort(Comparator.naturalOrder());

			if (matches.isEmpty())
			{
				return new ModelResolution.NotFound(requested);
			}
			if (matches.size() == 1)
			{
				return new ModelResolution.Found(matches.getFirst());
			}
			return new ModelResolution.Ambiguous(requested, List.copyOf(matches));
		}
		catch (IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			return new ModelResolution.Failed(message);
		}
	}

	private ModelResolution resolveQualifiedName(final String requested, final String qualifiedName)
	{
		try
		{
			headerIndex.refresh(projectRoot);
		}
		catch (IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			return new ModelResolution.Failed(message);
		}

		final var matches = headerIndex.headersByQualifiedName(qualifiedName)
									   .stream()
									   .map(DiskModelHeaderIndex.DiskModelHeader::path)
									   .filter(Objects::nonNull)
									   .map(path -> path.toAbsolutePath().normalize())
									   .sorted(Comparator.naturalOrder())
									   .toList();

		if (matches.isEmpty())
		{
			return new ModelResolution.NotFound(requested);
		}
		if (matches.size() == 1)
		{
			return new ModelResolution.Found(matches.getFirst());
		}
		return new ModelResolution.Ambiguous(requested, List.copyOf(matches));
	}
}
