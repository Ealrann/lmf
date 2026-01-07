package org.logoce.lmf.cli.edit;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorkspaceEditApplier
{
	public Map<Path, String> apply(final Map<Path, List<TextEdits.TextEdit>> editsByFile,
								   final Map<Path, String> sourcesByPath)
	{
		final var updated = new HashMap<Path, String>();

		for (final var entry : sourcesByPath.entrySet())
		{
			final var path = entry.getKey();
			final var source = entry.getValue();
			final var edits = editsByFile.get(path);
			if (edits == null || edits.isEmpty())
			{
				continue;
			}
			updated.put(path, TextEdits.apply(source, edits));
		}

		for (final var entry : editsByFile.entrySet())
		{
			if (!updated.containsKey(entry.getKey()) && entry.getValue() != null)
			{
				final var source = sourcesByPath.get(entry.getKey());
				if (source != null)
				{
					updated.put(entry.getKey(), TextEdits.apply(source, entry.getValue()));
				}
			}
		}

		return Map.copyOf(updated);
	}
}

