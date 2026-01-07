package org.logoce.lmf.cli.util;

import java.nio.file.Path;

public final class PathDisplay
{
	private PathDisplay()
	{
	}

	public static String display(final Path projectRoot, final Path path)
	{
		final var normalizedRoot = projectRoot.toAbsolutePath().normalize();
		final var normalizedPath = path.toAbsolutePath().normalize();
		if (normalizedPath.startsWith(normalizedRoot))
		{
			return normalizedRoot.relativize(normalizedPath).toString();
		}
		return normalizedPath.toString();
	}
}
