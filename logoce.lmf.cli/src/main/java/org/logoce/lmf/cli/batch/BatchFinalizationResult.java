package org.logoce.lmf.cli.batch;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record BatchFinalizationResult(int exitCode,
									  boolean wrote,
									  boolean dryRun,
									  boolean forcedWrite,
									  boolean validationFailed,
									  int stagedFileCount,
									  List<Path> files)
{
	public BatchFinalizationResult
	{
		Objects.requireNonNull(files, "files");
	}
}

