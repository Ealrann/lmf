package org.logoce.lmf.cli.edit;

import org.logoce.lmf.cli.ref.ObjectId;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;

import java.nio.file.Path;
import java.util.Objects;

public record ReferenceRewrite(Path path,
							   TextPositions.Span span,
							   String oldRaw,
							   String newRaw,
							   ObjectId resolvedTarget)
{
	public ReferenceRewrite
	{
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(oldRaw, "oldRaw");
		Objects.requireNonNull(newRaw, "newRaw");
		Objects.requireNonNull(resolvedTarget, "resolvedTarget");
	}
}

