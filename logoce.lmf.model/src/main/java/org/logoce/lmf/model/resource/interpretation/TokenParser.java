package org.logoce.lmf.model.resource.interpretation;

import org.logoce.lmf.model.lexer.ELMTokenType;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.resource.util.SoftIterator;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class TokenParser
{
	private final Iterator<PToken> iterator;

	public TokenParser(final Iterator<PToken> iterator)
	{
		this.iterator = iterator;
	}

	public PType createTypeToken()
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
			return new PType(Optional.of(firstValue), Optional.of(type.value()));
		}
		else
		{
			return new PType(Optional.empty(), Optional.of(firstValue));
		}
	}

	public Iterator<PFeature> valueIterator()
	{
		return new SoftIterator<>(this::createNextValueToken);
	}

	public Stream<PFeature> streamValues()
	{
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(valueIterator(), 0), false);
	}

	public Optional<PFeature> createNextValueToken()
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
			return Optional.of(PFeature.of(Optional.of(firstValue), nextValues));
		}
		else
		{
			assert firstToken.type() == ELMTokenType.VALUE || firstToken.type() == ELMTokenType.QUOTE;
			final var nextValues = nextContiguousValues(iterator);
			final var values = Stream.concat(Stream.of(firstValue), nextValues.stream()).toList();
			return Optional.of(PFeature.of(Optional.empty(), values));
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
					throw new IllegalStateException("Unmanaged case: " + type);
			}
		}
		return res;
	}
}
