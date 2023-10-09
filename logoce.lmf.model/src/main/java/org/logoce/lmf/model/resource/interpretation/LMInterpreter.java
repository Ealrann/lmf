package org.logoce.lmf.model.resource.interpretation;

import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lexer.ELMTokenType;
import org.logoce.lmf.model.lexer.LMLexer;
import org.logoce.lmf.model.resource.parsing.LMIterableLexer;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.util.Tree;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class LMInterpreter
{
	private static final LMIterableLexer LEXER = new LMIterableLexer();

	private final Map<String, Alias> aliases;

	public LMInterpreter(Map<String, Alias> aliases)
	{
		this.aliases = aliases;
	}

	public PGroup parse(final PNode pnode)
	{
		final var tokens = pnode.tokens();
		final var first = tokens.get(0);
		final var isAlias = isAliasDefinition(first);
		final var stream = isAlias ? tokens.stream() : tokens.stream().flatMap(this::mapAlias);
		final var builder = new TokenParser(stream.iterator());
		final var typeToken = builder.createTypeToken();
		final var valueTokens = builder.streamValues().toList();

		return new PGroup(typeToken, List.copyOf(valueTokens));
	}

	public PGroup parseTreeNode(final Tree<PNode> treeNode)
	{
		return parse(treeNode.data());
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
