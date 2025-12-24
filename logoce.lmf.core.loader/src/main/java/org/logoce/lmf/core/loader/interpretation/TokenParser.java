package org.logoce.lmf.core.loader.interpretation;

import org.logoce.lmf.core.loader.api.lexer.ELMTokenType;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;
import org.logoce.lmf.core.loader.util.SoftIterator;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class TokenParser
{
	private final Iterator<PToken> iterator;
	private final Deque<PToken> pushedBack = new ArrayDeque<>();

	public TokenParser(final Iterator<PToken> iterator)
	{
		this.iterator = iterator;
	}

	public PType createTypeToken()
	{
		final var firstToken = nextToken();
		final var firstValue = firstToken.value();
		assert firstToken.type() == ELMTokenType.TYPE_NAME || firstToken.type() == ELMTokenType.TYPE;
		if (firstToken.type() == ELMTokenType.TYPE_NAME)
		{
			final var assign = nextToken();
			assert assign.type() == ELMTokenType.ASSIGN;
			final var type = nextToken();
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
		final var tokenInfo = nextNotEmpty.get();
		final var firstToken = tokenInfo.token();
		final var firstValue = firstToken.value();
		if (firstToken.type() == ELMTokenType.VALUE_NAME)
		{
			final var assign = nextToken();
			assert assign.type() == ELMTokenType.ASSIGN;
			final var nextValues = nextContiguousValues(false);
			final var forceAttribute = shouldForceAttribute(nextValues.values(), nextValues.firstQuoted());
			return Optional.of(PFeature.of(Optional.of(firstValue), nextValues.values(), forceAttribute));
		}
		else
		{
			assert firstToken.type() == ELMTokenType.VALUE || firstToken.type() == ELMTokenType.QUOTE;
			final var nextValues = nextContiguousValues(tokenInfo.inQuote());
			final var values = Stream.concat(Stream.of(firstValue), nextValues.values().stream()).toList();
			final var forceAttribute = shouldForceAttribute(values, tokenInfo.inQuote());
			return Optional.of(PFeature.of(Optional.empty(), values, forceAttribute));
		}
	}

	private Optional<TokenInfo> nextNotEmpty()
	{
		boolean inQuote = false;
		while (hasNextToken())
		{
			final var next = nextToken();
			final var type = next.type();
			if (type == ELMTokenType.WHITE_SPACE)
			{
				continue;
			}
			if (type == ELMTokenType.QUOTE)
			{
				inQuote = !inQuote;
				continue;
			}
			return Optional.of(new TokenInfo(next, inQuote));
		}
		return Optional.empty();
	}

	private ValueTokens nextContiguousValues(final boolean startInQuote)
	{
		final List<String> res = new ArrayList<>();
		int quoteCount = 0;
		boolean inQuote = startInQuote;
		boolean firstValueSeen = false;
		boolean firstValueQuoted = false;
		boolean afterListSeparator = false;

		_while:
		while (hasNextToken())
		{
			final var token = nextToken();
			final var type = token.type();
			switch (type)
			{
				case WHITE_SPACE:
					if (afterListSeparator)
					{
						continue;
					}
					final var nextNonWhitespace = peekNextNonWhitespace();
					if (nextNonWhitespace.isPresent() &&
						nextNonWhitespace.get().type() == ELMTokenType.LIST_SEPARATOR)
					{
						continue;
					}
					break _while;
				case QUOTE:
					quoteCount++;
					inQuote = !inQuote;
					continue;
				case LIST_SEPARATOR:
					afterListSeparator = true;
					continue;
				case VALUE:
					res.add(token.value());
					if (!firstValueSeen)
					{
						firstValueQuoted = inQuote;
						firstValueSeen = true;
					}
					afterListSeparator = false;
					break;
				default:
					throw new IllegalStateException("Unmanaged case: " + type);
			}
		}

		if (res.isEmpty() && quoteCount >= 2 && !startInQuote)
		{
			// Interpret a pair of quotes with no VALUE tokens (e.g. defaultValue="")
			// as an explicit empty string, which is a valid attribute value.
			res.add("");
		}
		return new ValueTokens(res, firstValueQuoted);
	}

	private boolean hasNextToken()
	{
		return !pushedBack.isEmpty() || iterator.hasNext();
	}

	private PToken nextToken()
	{
		return pushedBack.isEmpty() ? iterator.next() : pushedBack.removeFirst();
	}

	private void pushBack(final PToken token)
	{
		pushedBack.addFirst(token);
	}

	private Optional<PToken> peekNextNonWhitespace()
	{
		while (hasNextToken())
		{
			final var token = nextToken();
			if (token.type() == ELMTokenType.WHITE_SPACE)
			{
				continue;
			}
			pushBack(token);
			return Optional.of(token);
		}
		return Optional.empty();
	}

	private record TokenInfo(PToken token, boolean inQuote)
	{
	}

	private record ValueTokens(List<String> values, boolean firstQuoted)
	{
	}

	private static boolean shouldForceAttribute(final List<String> values, final boolean firstQuoted)
	{
		if (!firstQuoted || values.isEmpty()) return false;
		final var firstValue = values.getFirst();
		for (int i = 0; i < firstValue.length(); i++)
		{
			if (firstValue.charAt(i) != '.')
			{
				return false;
			}
		}
		return !firstValue.isEmpty();
	}
}
