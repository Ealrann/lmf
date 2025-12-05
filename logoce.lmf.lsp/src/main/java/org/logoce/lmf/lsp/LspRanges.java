package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.util.TextPositions;

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

	public static Range forToken(final CharSequence source,
								 final PToken token)
	{
		final int startOffset = token.offset();
		final int endOffset = startOffset + Math.max(1, token.length());
		return forOffsets(source, startOffset, endOffset);
	}
}

