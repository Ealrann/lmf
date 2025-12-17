package org.logoce.lmf.core.loader.interpretation;

import org.logoce.lmf.core.lang.Alias;
import org.logoce.lmf.core.loader.api.lexer.ELMTokenType;
import org.logoce.lmf.core.loader.lexer.LMLexer;
import org.logoce.lmf.core.loader.api.text.parsing.LMIterableLexer;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;
import org.logoce.lmf.core.util.tree.BasicTree;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class LMInterpreter<I extends PNode>
{
	private static final LMIterableLexer LEXER = new LMIterableLexer();

	private final Map<String, Alias> aliases;

	public LMInterpreter(Map<String, Alias> aliases)
	{
		this.aliases = aliases;
	}

	public PGroup<I> interpretTreeNode(final BasicTree<I, ?> treeNode)
	{
		return interpret(treeNode.data());
	}

	public PGroup<I> interpret(final I pnode)
	{
		final var tokens = pnode.tokens();
		final var first = tokens.get(0);
		final var isAlias = isAliasDefinition(first);
		final var stream = isAlias ? tokens.stream() : tokens.stream().flatMap(this::mapAlias);
		final var builder = new TokenParser(stream.iterator());
		final var typeToken = builder.createTypeToken();
		final var valueTokens = builder.streamValues().toList();

		return new PGroup<>(pnode, typeToken, List.copyOf(valueTokens));
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
			   value.equals("Aliases");
	}
}
