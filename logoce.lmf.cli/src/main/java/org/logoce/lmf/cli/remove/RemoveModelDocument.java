package org.logoce.lmf.cli.remove;

import org.logoce.lmf.core.loader.api.loader.model.LmDocument;

import java.nio.file.Path;
import java.util.Objects;

public record RemoveModelDocument(Path path, LmDocument document)
{
	public RemoveModelDocument
	{
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(document, "document");
	}
}
