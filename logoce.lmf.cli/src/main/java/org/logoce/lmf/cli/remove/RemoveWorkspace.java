package org.logoce.lmf.cli.remove;

import java.util.List;
import java.util.Objects;

public record RemoveWorkspace(RemoveModelDocument targetDocument,
							  List<RemoveModelDocument> documents)
{
	public RemoveWorkspace
	{
		Objects.requireNonNull(targetDocument, "targetDocument");
		Objects.requireNonNull(documents, "documents");
	}
}
