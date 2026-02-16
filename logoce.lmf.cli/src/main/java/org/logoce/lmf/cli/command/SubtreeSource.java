package org.logoce.lmf.cli.command;

import java.nio.file.Path;
import java.util.Objects;

sealed interface SubtreeSource permits SubtreeSource.Inline, SubtreeSource.File, SubtreeSource.Stdin
{
	record Inline(String value) implements SubtreeSource
	{
		public Inline
		{
			Objects.requireNonNull(value, "value");
		}
	}

	record File(Path path) implements SubtreeSource
	{
		public File
		{
			Objects.requireNonNull(path, "path");
		}
	}

	record Stdin() implements SubtreeSource
	{
	}
}
