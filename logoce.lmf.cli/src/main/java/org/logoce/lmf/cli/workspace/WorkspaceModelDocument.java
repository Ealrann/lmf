package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.core.loader.api.loader.model.LmDocument;

import java.nio.file.Path;
import java.util.Objects;

public record WorkspaceModelDocument(String qualifiedName,
									 Path path,
									 LmDocument document)
{
	public WorkspaceModelDocument
	{
		Objects.requireNonNull(qualifiedName, "qualifiedName");
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(document, "document");
	}
}

