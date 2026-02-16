package org.logoce.lmf.cli.edit;

import org.logoce.lmf.core.loader.api.lexer.ELMTokenType;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class FeatureValueSpanIndex
{
	private final List<FeatureSpan> features;

	private FeatureValueSpanIndex(final List<FeatureSpan> features)
	{
		this.features = List.copyOf(features);
	}

	public static FeatureValueSpanIndex build(final List<PToken> tokens, final CharSequence source)
	{
		Objects.requireNonNull(tokens, "tokens");
		Objects.requireNonNull(source, "source");

		final var scanner = new TokenScanner(tokens);
		skipTypeToken(scanner);

		final var features = new ArrayList<FeatureSpan>();
		TokenInfo tokenInfo;

		while ((tokenInfo = nextNotEmpty(scanner)) != null)
		{
			final var token = tokenInfo.token();
			if (token.type() == ELMTokenType.VALUE_NAME)
			{
				final var assignmentTokens = new ArrayList<PToken>();
				assignmentTokens.add(token);

				final var assign = nextNonWhitespace(scanner, assignmentTokens);
				if (assign == null || assign.type() != ELMTokenType.ASSIGN)
				{
					break;
				}
				assignmentTokens.add(assign);

				final var values = parseValues(scanner, false, null, assignmentTokens, source);
				final var span = spanForTokens(assignmentTokens, source);
				features.add(new FeatureSpan(token.value(), values, span));
			}
			else if (token.type() == ELMTokenType.VALUE)
			{
				final var assignmentTokens = new ArrayList<PToken>();
				final var values = parseValues(scanner, tokenInfo.inQuote(), token, assignmentTokens, source);
				final var span = spanForTokens(assignmentTokens, source);
				features.add(new FeatureSpan(null, values, span));
			}
		}

		return new FeatureValueSpanIndex(features);
	}

	public List<FeatureSpan> features()
	{
		return features;
	}

	public FeatureSpan featureAt(final int index)
	{
		if (index < 0 || index >= features.size())
		{
			return null;
		}
		return features.get(index);
	}

	public List<FeatureSpan> featuresByName(final String name)
	{
		final var matches = new ArrayList<FeatureSpan>();
		for (final var feature : features)
		{
			if (Objects.equals(feature.name(), name))
			{
				matches.add(feature);
			}
		}
		return List.copyOf(matches);
	}

	public Optional<ValueSpan> findValueSpan(final String featureName, final String rawValue)
	{
		for (final var feature : features)
		{
			if (!Objects.equals(feature.name(), featureName))
			{
				continue;
			}
			for (final var value : feature.values())
			{
				if (Objects.equals(value.raw(), rawValue))
				{
					return Optional.of(value);
				}
			}
		}
		return Optional.empty();
	}

	public record FeatureSpan(String name, List<ValueSpan> values, TextPositions.Span assignmentSpan)
	{
	}

	public record ValueSpan(String raw, TextPositions.Span span, boolean quoted)
	{
	}

	private static void skipTypeToken(final TokenScanner scanner)
	{
		final var first = nextNotEmpty(scanner);
		if (first == null)
		{
			return;
		}

		final var token = first.token();
		if (token.type() == ELMTokenType.TYPE)
		{
			return;
		}
		if (token.type() != ELMTokenType.TYPE_NAME)
		{
			scanner.pushBack(token);
			return;
		}

		if (!"type".equals(token.value()))
		{
			return;
		}

		final var assign = nextNonWhitespace(scanner, null);
		if (assign == null)
		{
			return;
		}
		if (assign.type() != ELMTokenType.ASSIGN)
		{
			scanner.pushBack(assign);
			return;
		}

		final var typeToken = nextNonWhitespace(scanner, null);
		if (typeToken == null)
		{
			return;
		}
	}

	private static TokenInfo nextNotEmpty(final TokenScanner scanner)
	{
		boolean inQuote = false;
		while (scanner.hasNext())
		{
			final var token = scanner.next();
			switch (token.type())
			{
				case WHITE_SPACE -> {}
				case QUOTE -> inQuote = !inQuote;
				default -> {
					return new TokenInfo(token, inQuote);
				}
			}
		}
		return null;
	}

	private static PToken nextNonWhitespace(final TokenScanner scanner, final List<PToken> assignmentTokens)
	{
		while (scanner.hasNext())
		{
			final var token = scanner.next();
			if (token.type() == ELMTokenType.WHITE_SPACE)
			{
				if (assignmentTokens != null)
				{
					assignmentTokens.add(token);
				}
				continue;
			}
			return token;
		}
		return null;
	}

	private static List<ValueSpan> parseValues(final TokenScanner scanner,
											   final boolean startInQuote,
											   final PToken firstValueToken,
											   final List<PToken> assignmentTokens,
											   final CharSequence source)
	{
		final var values = new ArrayList<ValueSpan>();
		boolean inQuote = startInQuote;
		boolean afterListSeparator = false;
		boolean firstValueSeen = false;
		int quoteCount = 0;
		PToken firstQuote = null;
		PToken lastQuote = null;

		if (firstValueToken != null)
		{
			assignmentTokens.add(firstValueToken);
			values.add(valueSpanFor(firstValueToken, inQuote, source));
			firstValueSeen = true;
		}

		while (scanner.hasNext())
		{
			final var token = scanner.next();
			switch (token.type())
			{
				case WHITE_SPACE -> {
					if (afterListSeparator)
					{
						assignmentTokens.add(token);
						continue;
					}
					final var nextNonWhitespace = scanner.peekNextNonWhitespace();
					if (nextNonWhitespace.isPresent() &&
						nextNonWhitespace.get().type() == ELMTokenType.LIST_SEPARATOR)
					{
						assignmentTokens.add(token);
						continue;
					}
					return finalizeValues(values, startInQuote, quoteCount, firstQuote, lastQuote, source);
				}
				case QUOTE -> {
					assignmentTokens.add(token);
					quoteCount++;
					if (firstQuote == null)
					{
						firstQuote = token;
					}
					lastQuote = token;
					inQuote = !inQuote;
				}
				case LIST_SEPARATOR -> {
					assignmentTokens.add(token);
					afterListSeparator = true;
				}
				case VALUE -> {
					assignmentTokens.add(token);
					values.add(valueSpanFor(token, inQuote, source));
					if (!firstValueSeen)
					{
						firstValueSeen = true;
					}
					afterListSeparator = false;
				}
				default -> throw new IllegalStateException("Unmanaged token type: " + token.type());
			}
		}

		return finalizeValues(values, startInQuote, quoteCount, firstQuote, lastQuote, source);
	}

	private static List<ValueSpan> finalizeValues(final List<ValueSpan> values,
												  final boolean startInQuote,
												  final int quoteCount,
												  final PToken firstQuote,
												  final PToken lastQuote,
												  final CharSequence source)
	{
		if (!values.isEmpty())
		{
			return List.copyOf(values);
		}

		if (quoteCount >= 2 && !startInQuote && firstQuote != null && lastQuote != null)
		{
			final int offset = firstQuote.offset() + 1;
			final int length = Math.max(0, lastQuote.offset() - offset);
			final var span = spanForOffset(offset, length, source);
			values.add(new ValueSpan("", span, true));
		}

		return List.copyOf(values);
	}

	private static ValueSpan valueSpanFor(final PToken token, final boolean quoted, final CharSequence source)
	{
		final var span = spanForOffset(token.offset(), Math.max(0, token.length()), source);
		return new ValueSpan(token.value(), span, quoted);
	}

	private static TextPositions.Span spanForTokens(final List<PToken> tokens, final CharSequence source)
	{
		if (tokens == null || tokens.isEmpty())
		{
			return null;
		}

		int start = Integer.MAX_VALUE;
		int end = 0;

		for (final var token : tokens)
		{
			start = Math.min(start, token.offset());
			end = Math.max(end, token.offset() + Math.max(0, token.length()));
		}

		if (start == Integer.MAX_VALUE)
		{
			return null;
		}
		return spanForOffset(start, Math.max(0, end - start), source);
	}

	private static TextPositions.Span spanForOffset(final int offset, final int length, final CharSequence source)
	{
		final int line = TextPositions.lineFor(source, offset);
		final int column = TextPositions.columnFor(source, offset);
		return new TextPositions.Span(line, column, length, offset);
	}

	private record TokenInfo(PToken token, boolean inQuote)
	{
	}

	private static final class TokenScanner
	{
		private final List<PToken> tokens;
		private final Deque<PToken> pushedBack = new ArrayDeque<>();
		private int index = 0;

		private TokenScanner(final List<PToken> tokens)
		{
			this.tokens = tokens;
		}

		boolean hasNext()
		{
			return !pushedBack.isEmpty() || index < tokens.size();
		}

		PToken next()
		{
			return pushedBack.isEmpty() ? tokens.get(index++) : pushedBack.removeFirst();
		}

		void pushBack(final PToken token)
		{
			if (token != null)
			{
				pushedBack.addFirst(token);
			}
		}

		Optional<PToken> peekNextNonWhitespace()
		{
			final var buffer = new ArrayList<PToken>();
			PToken candidate = null;

			while (hasNext())
			{
				final var token = next();
				buffer.add(token);
				if (token.type() != ELMTokenType.WHITE_SPACE)
				{
					candidate = token;
					break;
				}
			}

			for (int i = buffer.size() - 1; i >= 0; i--)
			{
				pushBack(buffer.get(i));
			}

			return Optional.ofNullable(candidate);
		}
	}
}
