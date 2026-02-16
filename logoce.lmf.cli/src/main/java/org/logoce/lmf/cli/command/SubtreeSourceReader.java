package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.util.PathDisplay;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

final class SubtreeSourceReader
{
	private SubtreeSourceReader()
	{
	}

	static ReadResult read(final CliContext context, final SubtreeSource source)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(source, "source");

		if (source instanceof SubtreeSource.Inline inline)
		{
			return new ReadResult.Success(inline.value());
		}
		if (source instanceof SubtreeSource.File file)
		{
			return readFile(context, file.path());
		}
		if (source instanceof SubtreeSource.Stdin)
		{
			return readStdin();
		}
		return new ReadResult.Failure(ExitCodes.USAGE, "Unexpected subtree source type");
	}

	private static ReadResult readFile(final CliContext context, final Path path)
	{
		if (path == null)
		{
			return new ReadResult.Failure(ExitCodes.USAGE, "Missing subtree file path");
		}

		final var resolved = path.isAbsolute()
							 ? path.toAbsolutePath().normalize()
							 : context.projectRoot().resolve(path).toAbsolutePath().normalize();

		if (!Files.isRegularFile(resolved))
		{
			return new ReadResult.Failure(ExitCodes.USAGE,
										  "Subtree file not found: " + PathDisplay.display(context.projectRoot(), resolved));
		}

		try
		{
			return new ReadResult.Success(Files.readString(resolved, StandardCharsets.UTF_8));
		}
		catch (Exception e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			return new ReadResult.Failure(ExitCodes.INVALID,
										  "Failed to read subtree file: " + message);
		}
	}

	private static ReadResult readStdin()
	{
		try
		{
			final var buffer = new ByteArrayOutputStream();
			System.in.transferTo(buffer);
			return new ReadResult.Success(buffer.toString(StandardCharsets.UTF_8));
		}
		catch (Exception e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			return new ReadResult.Failure(ExitCodes.INVALID,
										  "Failed to read subtree from stdin: " + message);
		}
	}

	sealed interface ReadResult permits ReadResult.Success, ReadResult.Failure
	{
		record Success(String subtree) implements ReadResult
		{
			public Success
			{
				Objects.requireNonNull(subtree, "subtree");
			}
		}

		record Failure(int exitCode, String message) implements ReadResult
		{
			public Failure
			{
				Objects.requireNonNull(message, "message");
			}
		}
	}
}

