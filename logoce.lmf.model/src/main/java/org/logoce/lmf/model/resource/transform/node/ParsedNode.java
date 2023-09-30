package org.logoce.lmf.model.resource.transform.node;

import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lexer.ELMTokenType;
import org.logoce.lmf.model.lexer.LMLexer;
import org.logoce.lmf.model.resource.ptree.LMIterableLexer;
import org.logoce.lmf.model.resource.ptree.PToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public record ParsedNode(ParsedToken type, List<ParsedToken> values)
{
	public static final class Builder
	{
		private final Map<String, Alias> aliases;

		public Builder(Map<String, Alias> aliases)
		{
			this.aliases = aliases;
		}

		public ParsedNode build(final List<PToken> tokens)
		{
			final var first = tokens.get(0);
			final var isAlias = isAlias(first);
			final var stream = isAlias ? tokens.stream() : tokens.stream().flatMap(this::alias);
			final var it = stream.iterator();
			final var builder = new ParsedToken.Builder(it);
			final var typeToken = builder.createTypeToken();
			final var values = new ArrayList<ParsedToken>();
			while (it.hasNext())
			{
				builder.createValueToken().ifPresent(values::add);
			}

			return new ParsedNode(typeToken, List.copyOf(values));
		}

		private static boolean isAlias(final PToken first)
		{
			final var value = first.value();
			return value.equals(Alias.class.getSimpleName()) ||
				   value.equals(LMCoreDefinition.Features.MODEL.ALIASES.name());
		}

		private Stream<PToken> alias(final PToken word)
		{
			if (aliases.containsKey(word.value()))
			{
				final var value = aliases.get(word.value()).value();
				final var mapper = new AliasMapper(word.type() == ELMTokenType.TYPE);
				return mapper.map(value);
			}
			else
			{
				return Stream.of(word);
			}
		}

		private static final class AliasMapper
		{
			private static final Function<ELMTokenType, ELMTokenType> TYPE_MAPPER = input -> {
				if (input == ELMTokenType.VALUE_NAME) return ELMTokenType.TYPE_NAME;
				else if (input == ELMTokenType.VALUE) return ELMTokenType.TYPE;
				else return input;
			};

			private final boolean isType;

			private boolean first = true;

			private AliasMapper(final boolean isType)
			{
				this.isType = isType;
			}

			public Stream<PToken> map(String word)
			{
				final var lexer = new LMIterableLexer();
				final var initialState = first && isType ? LMLexer.WAITING_TYPE : 0;
				lexer.reset(word, initialState);
				first = false;
				return lexer.stream();
			}
		}
	}
}
