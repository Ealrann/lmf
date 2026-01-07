package org.logoce.lmf.cli.batch;

import java.nio.file.Path;
import java.util.Objects;

public record BatchValidationRoot(Path modelPath, boolean includeImporters)
{
	public BatchValidationRoot
	{
		Objects.requireNonNull(modelPath, "modelPath");
	}
}
