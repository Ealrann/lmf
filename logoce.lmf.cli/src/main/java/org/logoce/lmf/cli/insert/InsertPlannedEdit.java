package org.logoce.lmf.cli.insert;

import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.edit.ReferenceRewrite;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record InsertPlannedEdit(Map<Path, List<TextEdits.TextEdit>> editsByFile,
								List<ReferenceRewrite> rewrites)
{
	public InsertPlannedEdit
	{
		Objects.requireNonNull(editsByFile, "editsByFile");
		Objects.requireNonNull(rewrites, "rewrites");
	}
}
