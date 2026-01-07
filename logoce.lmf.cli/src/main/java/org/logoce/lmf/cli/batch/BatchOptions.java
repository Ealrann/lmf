package org.logoce.lmf.cli.batch;

import java.nio.file.Path;

public record BatchOptions(Path file,
						   boolean readFromStdin,
						   boolean dryRun,
						   boolean continueOnError,
						   boolean force,
						   ValidateMode validateMode,
						   String defaultModel)
{
	public enum ValidateMode
	{
		EACH,
		FINAL,
		NONE
	}
}

