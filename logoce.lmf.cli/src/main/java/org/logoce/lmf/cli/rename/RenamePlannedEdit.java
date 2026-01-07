package org.logoce.lmf.cli.rename;

import org.logoce.lmf.cli.edit.TextEdits;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record RenamePlannedEdit(Map<Path, List<TextEdits.TextEdit>> editsByFile, boolean changed)
{
	public RenamePlannedEdit
	{
		Objects.requireNonNull(editsByFile, "editsByFile");
	}
}

