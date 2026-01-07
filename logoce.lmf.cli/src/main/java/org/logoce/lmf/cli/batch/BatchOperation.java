package org.logoce.lmf.cli.batch;

import java.util.List;
import java.util.Objects;

public record BatchOperation(int index, int lineNumber, String command, List<String> args, String rawLine)
{
	public BatchOperation
	{
		if (index < 1)
		{
			throw new IllegalArgumentException("index must be >= 1");
		}
		if (lineNumber < 1)
		{
			throw new IllegalArgumentException("lineNumber must be >= 1");
		}
		Objects.requireNonNull(command, "command");
		Objects.requireNonNull(args, "args");
		rawLine = rawLine == null ? "" : rawLine;
	}
}

