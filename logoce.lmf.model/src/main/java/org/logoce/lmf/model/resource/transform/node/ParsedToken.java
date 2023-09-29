package org.logoce.lmf.model.resource.transform.node;

import org.logoce.lmf.model.lexer.ELMTokenType;
import org.logoce.lmf.model.resource.ptree.PToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ParsedToken
{
	Optional<String> name();
	List<String> values();

	default String firstToken()
	{
		return name().orElse(values().get(0));
	}

	final class Builder
	{
		private final Iterator<PToken> iterator;

		public Builder(final Iterator<PToken> iterator)
		{
			this.iterator = iterator;
		}

		public ParsedToken createTypeToken()
		{
			final var firstToken = iterator.next();
			final var firstValue = firstToken.value();
			assert firstToken.type() == ELMTokenType.TYPE_NAME || firstToken.type() == ELMTokenType.TYPE;
			if (firstToken.type() == ELMTokenType.TYPE_NAME)
			{
				final var assign = iterator.next();
				assert assign.type() == ELMTokenType.ASSIGN;
				final var type = iterator.next();
				assert type.type() == ELMTokenType.TYPE;
				return new ParsedNamedToken(firstValue, type.value());
			}
			else
			{
				return new ParsedSimpleToken(firstValue);
			}
		}

		public Optional<ParsedToken> createValueToken()
		{
			final var nextNotEmpty = nextNotEmpty();
			if (nextNotEmpty.isEmpty()) return Optional.empty();
			final var firstToken = nextNotEmpty.get();
			final var firstValue = firstToken.value();
			if (firstToken.type() == ELMTokenType.VALUE_NAME)
			{
				final var assign = iterator.next();
				assert assign.type() == ELMTokenType.ASSIGN;
				final var nextValues = nextContiguousValues(iterator);
				if (nextValues.size() == 1) return Optional.of(new ParsedNamedToken(firstValue, nextValues.get(0)));
				else return Optional.of(new ParsedNamedToken(firstValue, nextValues));
			}
			else
			{
				assert firstToken.type() == ELMTokenType.VALUE || firstToken.type() == ELMTokenType.QUOTE;
				final var nextValues = nextContiguousValues(iterator);
				final var values = Stream.concat(Stream.of(firstValue), nextValues.stream()).toList();
				if (values.size() == 1) return Optional.of(new ParsedSimpleToken(values.get(0)));
				else return Optional.of(new ParsedSimpleToken(values));
			}
		}

		private Optional<PToken> nextNotEmpty()
		{
			while (iterator.hasNext())
			{
				final var next = iterator.next();
				final var type = next.type();
				if (type != ELMTokenType.WHITE_SPACE && type != ELMTokenType.QUOTE)
				{
					return Optional.of(next);
				}
			}
			return Optional.empty();
		}

		private static List<String> nextContiguousValues(final Iterator<PToken> iterator)
		{
			final List<String> res = new ArrayList<>();
			_while:
			while (iterator.hasNext())
			{
				final var token = iterator.next();
				final var type = token.type();
				switch (type)
				{
					case WHITE_SPACE:
						break _while;
					case QUOTE:
					case LIST_SEPARATOR:
						continue;
					case VALUE:
						res.add(token.value());
						break;
					default:
						throw new IllegalStateException("Unmanaged case");
				}
			}
			return res;
		}

		private record ParsedSimpleToken(List<String> values) implements ParsedToken
		{
			public ParsedSimpleToken(String value)
			{
				this(List.of(value));
			}

			@Override
			public Optional<String> name()
			{
				return Optional.empty();
			}
		}

		private record ParsedNamedToken(Optional<String> name, List<String> values) implements ParsedToken
		{
			public ParsedNamedToken(String name, List<String> values)
			{
				this(Optional.of(name), values);
			}

			public ParsedNamedToken(String name, String value)
			{
				this(Optional.of(name), List.of(value));
			}
		}
	}
}
