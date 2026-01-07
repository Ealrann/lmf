package org.logoce.lmf.cli.format;

import org.logoce.lmf.core.lang.Named;
import org.logoce.lmf.core.loader.api.lexer.ELMTokenType;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;

import java.util.List;

public final class NodeNameResolver
{
	private NodeNameResolver()
	{
	}

	public static String resolve(final LinkNodeInternal<?, PNode, ?> node)
	{
		if (node == null)
		{
			return null;
		}

		try
		{
			final var built = node.build();
			if (built instanceof Named named)
			{
				final var name = named.name();
				return name == null || name.isBlank() ? null : name;
			}
		}
		catch (RuntimeException ignored)
		{
			return null;
		}

		return null;
	}

	public static String resolve(final PNode node)
	{
		if (node == null)
		{
			return null;
		}

		final var tokens = node.tokens();
		if (tokens == null || tokens.isEmpty())
		{
			return null;
		}

		final var explicit = resolveExplicitName(tokens);
		if (explicit != null)
		{
			return explicit;
		}

		final var significant = tokens.stream()
									  .filter(t -> t.type() != ELMTokenType.WHITE_SPACE)
									  .toList();
		if (significant.size() < 2)
		{
			return null;
		}

		final var candidate = significant.get(1);
		if (candidate.type() == ELMTokenType.VALUE_NAME)
		{
			return null;
		}
		return candidate.value();
	}

	private static String resolveExplicitName(final List<PToken> tokens)
	{
		for (int i = 0; i < tokens.size(); i++)
		{
			final var token = tokens.get(i);
			if (token.type() != ELMTokenType.VALUE_NAME || !Named.Features.NAME.name().equals(token.value()))
			{
				continue;
			}

			int cursor = i + 1;
			cursor = skipWhitespace(tokens, cursor);
			if (cursor < tokens.size() && tokens.get(cursor).type() == ELMTokenType.ASSIGN)
			{
				cursor = skipWhitespace(tokens, cursor + 1);
			}

			if (cursor < tokens.size() && tokens.get(cursor).type() == ELMTokenType.QUOTE)
			{
				cursor = skipWhitespace(tokens, cursor + 1);
			}

			if (cursor < tokens.size() && tokens.get(cursor).type() == ELMTokenType.VALUE)
			{
				return tokens.get(cursor).value();
			}
		}

		return null;
	}

	private static int skipWhitespace(final List<PToken> tokens, final int start)
	{
		int cursor = start;
		while (cursor < tokens.size() && tokens.get(cursor).type() == ELMTokenType.WHITE_SPACE)
		{
			cursor++;
		}
		return cursor;
	}
}
