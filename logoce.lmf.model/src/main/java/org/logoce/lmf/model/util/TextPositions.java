package org.logoce.lmf.model.util;

import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PToken;

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
}

