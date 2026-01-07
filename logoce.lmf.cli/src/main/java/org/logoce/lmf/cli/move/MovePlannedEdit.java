package org.logoce.lmf.cli.move;

import org.logoce.lmf.cli.edit.TextEdits;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record MovePlannedEdit(Map<Path, List<TextEdits.TextEdit>> editsByFile, boolean changed)
{
	public MovePlannedEdit
	{
		Objects.requireNonNull(editsByFile, "editsByFile");
	}
}
