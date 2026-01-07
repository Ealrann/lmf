package org.logoce.lmf.cli.workspace;

import java.nio.file.Path;
import java.util.List;

public sealed interface ModelResolution permits ModelResolution.Found,
											  ModelResolution.NotFound,
											  ModelResolution.Ambiguous,
											  ModelResolution.Failed
{
	record Found(Path path) implements ModelResolution
	{
	}

	record NotFound(String requested) implements ModelResolution
	{
	}

	record Ambiguous(String requested, List<Path> matches) implements ModelResolution
	{
	}

	record Failed(String message) implements ModelResolution
	{
	}
}
