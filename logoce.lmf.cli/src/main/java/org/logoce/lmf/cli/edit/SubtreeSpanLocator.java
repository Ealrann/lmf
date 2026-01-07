package org.logoce.lmf.cli.edit;

import org.logoce.lmf.core.loader.api.lexer.ELMTokenType;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.parsing.LMIterableLexer;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;

import java.util.List;

public final class SubtreeSpanLocator
{
	private SubtreeSpanLocator()
	{
	}

	public record Span(int startOffset, int endOffset)
	{
		public Span
		{
			if (startOffset < 0)
			{
				throw new IllegalArgumentException("startOffset must be >= 0");
			}
			if (endOffset < startOffset)
			{
				throw new IllegalArgumentException("endOffset must be >= startOffset");
			}
		}

		public int length()
		{
			return endOffset - startOffset;
		}
	}

	public static Span locate(final CharSequence source, final LinkNodeInternal<?, PNode, ?> node)
	{
		if (source == null || node == null)
		{
			return null;
		}

		final List<PToken> tokens = node.pNode() != null ? node.pNode().tokens() : List.of();
		if (tokens.isEmpty())
		{
			return null;
		}

		final int firstOffset = tokens.getFirst().offset();
		if (firstOffset <= 0 || firstOffset > source.length())
		{
			return null;
		}

		int start = firstOffset - 1;
		while (start >= 0 && Character.isWhitespace(source.charAt(start)))
		{
			start--;
		}
		if (start < 0 || source.charAt(start) != '(')
		{
			return null;
		}

		final int end = findMatchingCloseOffset(source, start);
		if (end <= start)
		{
			return null;
		}

		return new Span(start, end);
	}

	private static int findMatchingCloseOffset(final CharSequence source, final int startOffset)
	{
		final var slice = source.subSequence(startOffset, source.length());
		final var lexer = new LMIterableLexer();
		lexer.reset(slice, 0);

		int depth = 0;
		for (final PToken token : lexer)
		{
			if (token.type() == ELMTokenType.OPEN_NODE)
			{
				depth++;
				continue;
			}
			if (token.type() == ELMTokenType.CLOSE_NODE)
			{
				depth--;
				if (depth == 0)
				{
					return startOffset + token.offset() + Math.max(1, token.length());
				}
			}
		}
		return -1;
	}
}

