package org.logoce.lmf.cli.remove;

import org.logoce.lmf.cli.ref.ObjectId;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;

import java.nio.file.Path;
import java.util.Objects;

public record RemoveUnsetReference(Path path, TextPositions.Span span, String raw, ObjectId targetId)
{
	public RemoveUnsetReference
	{
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(span, "span");
		Objects.requireNonNull(targetId, "targetId");
	}
}
