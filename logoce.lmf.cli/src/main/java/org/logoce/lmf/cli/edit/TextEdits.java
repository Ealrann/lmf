package org.logoce.lmf.cli.edit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class TextEdits
{
	private TextEdits()
	{
	}

	public record TextEdit(int offset, int length, String replacement)
	{
		public TextEdit
		{
			if (offset < 0)
			{
				throw new IllegalArgumentException("offset must be >= 0");
			}
			if (length < 0)
			{
				throw new IllegalArgumentException("length must be >= 0");
			}
			replacement = replacement == null ? "" : replacement;
		}
	}

	public static String apply(final CharSequence source, final List<TextEdit> edits)
	{
		Objects.requireNonNull(source, "source");
		if (edits == null || edits.isEmpty())
		{
			return source.toString();
		}

		final var sorted = new ArrayList<TextEdit>(edits.size());
		for (final var edit : edits)
		{
			if (edit != null)
			{
				sorted.add(edit);
			}
		}
		sorted.sort(Comparator.comparingInt(TextEdit::offset).reversed());

		final int sourceLength = source.length();
		int nextAllowedEnd = sourceLength;

		for (final var edit : sorted)
		{
			final int start = edit.offset();
			final int end = start + edit.length();

			if (start > sourceLength || end > sourceLength)
			{
				throw new IllegalArgumentException("Edit out of bounds: [" + start + "," + end + ") for length " + sourceLength);
			}
			if (end > nextAllowedEnd)
			{
				throw new IllegalArgumentException("Overlapping edits are not supported");
			}

			nextAllowedEnd = start;
		}

		final var builder = new StringBuilder(sourceLength);
		int cursor = sourceLength;

		for (final var edit : sorted)
		{
			final int start = edit.offset();
			final int end = start + edit.length();

			if (end < cursor)
			{
				builder.insert(0, source.subSequence(end, cursor));
			}
			builder.insert(0, edit.replacement());
			cursor = start;
		}

		if (cursor > 0)
		{
			builder.insert(0, source.subSequence(0, cursor));
		}

		return builder.toString();
	}
}

