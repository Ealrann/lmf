package org.logoce.lmf.model.resource.transform.parsing;

import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lexer.ELMTokenType;
import org.logoce.lmf.model.lexer.LMLexer;
import org.logoce.lmf.model.resource.ptree.LMIterableLexer;
import org.logoce.lmf.model.resource.ptree.PToken;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class NodeParser
{
	private static final LMIterableLexer LEXER = new LMIterableLexer();

	private final Map<String, Alias> aliases;

	public NodeParser(Map<String, Alias> aliases)
	{
		this.aliases = aliases;
	}

	public ParsedNode parse(final List<PToken> tokens)
	{
		final var first = tokens.get(0);
		final var isAlias = isAliasDefinition(first);
		final var stream = isAlias ? tokens.stream() : tokens.stream().flatMap(this::mapAlias);
		final var builder = new TokenParser(stream.iterator());
		final var typeToken = builder.createTypeToken();
		final var valueTokens = builder.streamValues().toList();

		return new ParsedNode(typeToken, List.copyOf(valueTokens));
	}

	private Stream<PToken> mapAlias(final PToken word)
	{
		if (aliases.containsKey(word.value()))
		{
			final var value = aliases.get(word.value()).value();
			final var isType = word.type() == ELMTokenType.TYPE;
			final var initialState = isType ? LMLexer.WAITING_TYPE : 0;
			LEXER.reset(value, initialState);
			return LEXER.stream();
		}
		else
		{
			return Stream.of(word);
		}
	}

	private static boolean isAliasDefinition(final PToken first)
	{
		final var value = first.value();
		return value.equals(Alias.class.getSimpleName()) ||
			   value.equals(LMCoreDefinition.Features.MODEL.ALIASES.name());
	}
}
