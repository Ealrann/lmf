package org.logoce.lmf.cli.remove;

import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.edit.ReferenceRewrite;
import org.logoce.lmf.cli.ref.ObjectId;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record RemovePlannedEdit(Map<Path, List<TextEdits.TextEdit>> editsByFile,
								List<RemoveUnsetReference> unsets,
								List<ReferenceRewrite> rewrites,
								ObjectId removedId)
{
	public RemovePlannedEdit
	{
		Objects.requireNonNull(editsByFile, "editsByFile");
		Objects.requireNonNull(unsets, "unsets");
		Objects.requireNonNull(rewrites, "rewrites");
		Objects.requireNonNull(removedId, "removedId");
	}
}
