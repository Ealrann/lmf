package org.logoce.lmf.core.loader.api.loader.util;

import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;

import java.util.Arrays;
import java.util.Optional;

/**
 * Helper methods to map character offsets in a {@link CharSequence}
 * to 1-based line/column positions, and to compute simple spans for
 * {@link PNode} instances. Shared between diagnostics-producing code
 * so that tools and LSP integrations can rely on consistent mapping.
 */
public final class TextPositions
{
	private TextPositions()
	{
	}

	public static final class LineIndex
	{
		private final int[] lineOffsets;
		private final int length;

		private LineIndex(final int[] lineOffsets, final int length)
		{
			this.lineOffsets = lineOffsets;
			this.length = length;
		}

		public int lineFor(final int offset)
		{
			final int clamped = Math.max(0, Math.min(offset, length));
			final int idx = Arrays.binarySearch(lineOffsets, clamped);
			if (idx >= 0)
			{
				return idx + 1;
			}
			final int insertionPoint = -idx - 1;
			return Math.max(1, insertionPoint);
		}

		public int columnFor(final int offset)
		{
			final int clamped = Math.max(0, Math.min(offset, length));
			final int line = lineFor(clamped);
			final int lineStart = lineOffsets[Math.max(0, line - 1)];
			return Math.max(1, clamped - lineStart + 1);
		}
	}

	public static LineIndex index(final CharSequence text)
	{
		if (text == null || text.length() == 0)
		{
			return new LineIndex(new int[]{0}, 0);
		}

		final int length = text.length();
		int[] offsets = new int[Math.max(16, length / 32)];
		int count = 0;
		offsets[count++] = 0;

		for (int i = 0; i < length; i++)
		{
			if (text.charAt(i) == '\n')
			{
				if (count == offsets.length)
				{
					offsets = Arrays.copyOf(offsets, offsets.length * 2);
				}
				if (i + 1 <= length)
				{
					offsets[count++] = i + 1;
				}
			}
		}

		return new LineIndex(Arrays.copyOf(offsets, count), length);
	}

	public record Span(int line, int column, int length, int offset)
	{
	}

	public static int lineFor(final CharSequence text, final int offset)
	{
		int line = 1;
		for (int i = 0; i < offset && i < text.length(); i++)
		{
			if (text.charAt(i) == '\n') line++;
		}
		return line;
	}

	public static int columnFor(final CharSequence text, final int offset)
	{
		int col = 1;
		for (int i = offset - 1; i >= 0 && i < text.length(); i--)
		{
			if (text.charAt(i) == '\n') break;
			col++;
		}
		return col;
	}

	/**
	 * Compute the 0-based character offset for the given 1-based line and column.
	 * Returns -1 when the requested line is beyond the end of the text.
	 */
	public static int offsetFor(final CharSequence text, final int line, final int column)
	{
		if (line < 1 || column < 1)
		{
			return -1;
		}

		int currentLine = 1;
		int i = 0;
		final int length = text.length();
		while (i < length && currentLine < line)
		{
			if (text.charAt(i) == '\n')
			{
				currentLine++;
			}
			i++;
		}

		if (currentLine != line)
		{
			return -1;
		}

		final int offset = i + (column - 1);
		return Math.min(offset, length);
	}

	public static Span spanOf(final PNode node, final CharSequence source)
	{
		final Optional<PToken> firstToken = node.tokens().stream().findFirst();
		if (firstToken.isEmpty())
		{
			return new Span(1, 1, 1, 0);
		}

		final var tok = firstToken.get();
		final int offset = tok.offset();
		final int length = Math.max(1, tok.length());
		final int line = lineFor(source, offset);
		final int column = columnFor(source, offset);
		return new Span(line, column, length, offset);
	}

	public static Span spanOf(final PToken token, final CharSequence source)
	{
		if (token == null)
		{
			return new Span(1, 1, 1, 0);
		}

		final int offset = token.offset();
		final int length = Math.max(1, token.length());
		final int line = lineFor(source, offset);
		final int column = columnFor(source, offset);
		return new Span(line, column, length, offset);
	}
}
