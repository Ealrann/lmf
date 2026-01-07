package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;

public final class LspRanges
{
	private LspRanges()
	{
	}

	public static Range forOffsets(final CharSequence source,
								   final int startOffset,
								   final int endOffset)
	{
		final int startLine = Math.max(0, TextPositions.lineFor(source, startOffset) - 1);
		final int startChar = Math.max(0, TextPositions.columnFor(source, startOffset) - 1);
		final int endLine = Math.max(0, TextPositions.lineFor(source, endOffset) - 1);
		final int endChar = Math.max(0, TextPositions.columnFor(source, endOffset) - 1);

		final var startPos = new Position(startLine, startChar);
		final var endPos = new Position(endLine, endChar);
		return new Range(startPos, endPos);
	}

	public static Range forSpan(final CharSequence source,
								final TextPositions.Span span)
	{
		final int startOffset = span.offset();
		final int endOffset = startOffset + Math.max(1, span.length());
		return forOffsets(source, startOffset, endOffset);
	}

	public static Range forSpan(final TextPositions.Span span)
	{
		if (span == null)
		{
			final var pos = new Position(0, 0);
			return new Range(pos, pos);
		}

		final int line = Math.max(0, span.line() - 1);
		final int startChar = Math.max(0, span.column() - 1);
		final int endChar = startChar + Math.max(1, span.length());

		final var startPos = new Position(line, startChar);
		final var endPos = new Position(line, endChar);
		return new Range(startPos, endPos);
	}

	public static Range forToken(final CharSequence source,
								 final PToken token)
	{
		final int startOffset = token.offset();
		final int endOffset = startOffset + Math.max(1, token.length());
		return forOffsets(source, startOffset, endOffset);
	}
}
