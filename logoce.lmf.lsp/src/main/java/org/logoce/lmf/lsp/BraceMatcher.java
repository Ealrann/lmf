package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;

import java.util.List;

final class BraceMatcher
{
	private BraceMatcher()
	{
	}

	static List<DocumentHighlight> matchParenthesis(final CharSequence source, final Position pos)
	{
		int offset = offsetForPosition(source, pos);
		if (offset < 0 || offset > source.length())
		{
			return List.of();
		}

		char ch = offset < source.length() ? source.charAt(offset) : '\0';
		if (ch != '(' && ch != ')')
		{
			final int prev = offset - 1;
			if (prev >= 0)
			{
				final char prevCh = source.charAt(prev);
				if (prevCh == '(' || prevCh == ')')
				{
					offset = prev;
					ch = prevCh;
				}
			}
		}

		if (ch != '(' && ch != ')')
		{
			return List.of();
		}

		final int mateOffset = ch == '('
							   ? findMatchingParenForward(source, offset)
							   : findMatchingParenBackward(source, offset);
		if (mateOffset < 0)
		{
			return List.of();
		}

		final var firstRange = LspRanges.forOffsets(source, offset, offset + 1);
		final var secondRange = LspRanges.forOffsets(source, mateOffset, mateOffset + 1);

		final var first = new DocumentHighlight(firstRange, DocumentHighlightKind.Text);
		final var second = new DocumentHighlight(secondRange, DocumentHighlightKind.Text);
		return List.of(first, second);
	}

	private static int offsetForPosition(final CharSequence source, final Position pos)
	{
		final int line = pos.getLine() + 1;
		final int column = pos.getCharacter() + 1;
		return TextPositions.offsetFor(source, line, column);
	}

	private static int findMatchingParenForward(final CharSequence source, final int startOffset)
	{
		int depth = 0;
		for (int i = startOffset; i < source.length(); i++)
		{
			final char c = source.charAt(i);
			if (c == '(')
			{
				depth++;
			}
			else if (c == ')')
			{
				depth--;
				if (depth == 0)
				{
					return i;
				}
			}
		}
		return -1;
	}

	private static int findMatchingParenBackward(final CharSequence source, final int startOffset)
	{
		int depth = 0;
		for (int i = startOffset; i >= 0; i--)
		{
			final char c = source.charAt(i);
			if (c == ')')
			{
				depth++;
			}
			else if (c == '(')
			{
				depth--;
				if (depth == 0)
				{
					return i;
				}
			}
		}
		return -1;
	}
}
