package org.logoce.lmf.cli.workspace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ModelLocator
{
	private final Path projectRoot;

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
			final List<Path> matches;
			try (final var walk = Files.walk(projectRoot))
			{
				matches = walk.filter(Files::isRegularFile)
							  .filter(path -> path.getFileName().toString().equals(fileName.toString()))
							  .map(path -> path.toAbsolutePath().normalize())
							  .sorted(Comparator.naturalOrder())
							  .toList();
			}

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
}
