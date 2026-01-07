package org.logoce.lmf.cli.batch;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Objects;

public final class BatchRunner
{
	public int run(final CliContext context, final BatchOptions options, final Reader reader)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(options, "options");
		Objects.requireNonNull(reader, "reader");

		final List<BatchOperation> operations;
		try
		{
			operations = new BatchScriptReader().read(reader);
		}
		catch (BatchJsonParser.BatchParseException e)
		{
			context.err().println("Batch parse error at line " + e.lineNumber() + ": " + e.getMessage());
			return ExitCodes.USAGE;
		}
		catch (IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			context.err().println("Failed to read batch script: " + message);
			return ExitCodes.INVALID;
		}

		return new BatchCoordinator().run(context, options, operations);
	}
}
