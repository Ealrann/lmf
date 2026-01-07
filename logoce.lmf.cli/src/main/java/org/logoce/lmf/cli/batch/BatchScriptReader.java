package org.logoce.lmf.cli.batch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BatchScriptReader
{
	public List<BatchOperation> read(final Reader reader) throws IOException
	{
		Objects.requireNonNull(reader, "reader");

		final var buffered = reader instanceof BufferedReader br ? br : new BufferedReader(reader);
		final var operations = new ArrayList<BatchOperation>();

		String line;
		int lineNumber = 0;
		int opIndex = 0;

		while ((line = buffered.readLine()) != null)
		{
			lineNumber++;
			final var trimmed = line.strip();
			if (trimmed.isEmpty() || trimmed.startsWith("#"))
			{
				continue;
			}

			final var parsed = BatchJsonParser.parseOperation(trimmed, lineNumber);
			opIndex++;
			operations.add(new BatchOperation(opIndex,
											  lineNumber,
											  parsed.command(),
											  parsed.args(),
											  line));
		}

		return List.copyOf(operations);
	}
}

