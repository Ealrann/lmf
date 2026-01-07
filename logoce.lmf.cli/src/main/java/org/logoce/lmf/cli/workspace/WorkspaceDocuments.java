package org.logoce.lmf.cli.workspace;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record WorkspaceDocuments(WorkspaceModelDocument targetDocument,
								 List<WorkspaceModelDocument> documents,
								 Map<Path, String> sourcesByPath)
{
	public WorkspaceDocuments
	{
		Objects.requireNonNull(targetDocument, "targetDocument");
		Objects.requireNonNull(documents, "documents");
		Objects.requireNonNull(sourcesByPath, "sourcesByPath");
	}
}

