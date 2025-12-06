package org.logoce.lmf.model.resource.parsing;

import org.logoce.lmf.model.lexer.ELMTokenType;
import org.logoce.lmf.model.lexer.LMLexer;
import org.logoce.lmf.model.resource.parsing.LexerException;
import org.logoce.lmf.model.resource.util.SoftIterator;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class LMIterableLexer implements Iterable<PToken>
{
	private final LMLexer lexer = new LMLexer(null);
	private final Deque<PToken> pendingTokens = new ArrayDeque<>();
	private CharSequence text = "";
	private int offset = 0;
	private int lastLength = 0;
	private boolean lexerNeedsReset = false;
	private int resetIndex = 0;

	public LMIterableLexer()
	{
	}

	@Override
	public Iterator<PToken> iterator()
	{
		return new SoftIterator<>(this::nextToken);
	}

	private Optional<PToken> nextToken()
	{
		if (pendingTokens.isEmpty() == false)
		{
			final var token = pendingTokens.removeFirst();
			lastLength = token.length();
			return Optional.of(token);
		}

		if (lexerNeedsReset)
		{
			lexer.reset(text, resetIndex, text.length(), LMLexer.YYINITIAL);
			offset = resetIndex;
			lastLength = 0;
			lexerNeedsReset = false;
		}

		try
		{
			final var token = lexer.next();
			if (token == null) return Optional.empty();
			final String text = lexer.yytext().toString();
			final int start = offset;
			lastLength = text.length();
			offset += lastLength;

			if (token == ELMTokenType.QUOTE)
			{
				handleForcedValue(start);
			}

			if (token == ELMTokenType.BAD_CHARACTER && "^".equals(text))
			{
				final var valueToken = handleContextValue(start);
				return Optional.of(valueToken);
			}

			return Optional.of(new PToken(text, token, start, text.length()));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void reset(final CharSequence text, final int initialState)
	{
		this.text = text;
		pendingTokens.clear();
		lexer.reset(text, 0, text.length(), initialState);
		offset = 0;
		lastLength = 0;
		lexerNeedsReset = false;
	}

	public Stream<PToken> stream()
	{
		return StreamSupport.stream(spliterator(), false);
	}

	public int currentOffset() {
		return offset;
	}

	public int lastLength() {
		return lastLength;
	}

	private void handleForcedValue(final int quoteOffset)
	{
		final int contentStart = quoteOffset + 1;
		final var forcedValue = readForcedValue(contentStart);
		final int closingQuoteOffset = forcedValue.endQuoteOffset();

		pendingTokens.addLast(new PToken(forcedValue.value(), ELMTokenType.VALUE, contentStart, forcedValue.rawLength()));
		pendingTokens.addLast(new PToken("\"", ELMTokenType.QUOTE, closingQuoteOffset, 1));

		lexerNeedsReset = true;
		resetIndex = closingQuoteOffset + 1;
	}

	private PToken handleContextValue(final int caretOffset)
	{
		final int length = text.length();
		int i = caretOffset + 1;

		while (i < length)
		{
			final char c = text.charAt(i);
			if (!Character.isJavaIdentifierPart(c))
			{
				break;
			}
			i++;
		}

		final String value = text.subSequence(caretOffset, i).toString();
		final int rawLength = i - caretOffset;

		lexerNeedsReset = true;
		resetIndex = i;

		offset = i;
		lastLength = rawLength;

		return new PToken(value, ELMTokenType.VALUE, caretOffset, rawLength);
	}

	private ForcedValue readForcedValue(final int startIndex)
	{
		final int length = text.length();
		final var builder = new StringBuilder();
		boolean escaped = false;
		int i = startIndex;

		for (; i < length; i++)
		{
			final char c = text.charAt(i);
			if (escaped)
			{
				if (c == '"' || c == '\\')
				{
					builder.append(c);
				}
				else
				{
					// keep the escape for non-quote characters so patterns like \b are preserved
					builder.append('\\').append(c);
				}
				escaped = false;
			}
			else if (c == '\\')
			{
				escaped = true;
			}
			else if (c == '"')
			{
				break;
			}
			else
			{
				builder.append(c);
			}
		}

		if (i >= length)
		{
			throw new LexerException("Unterminated forced value");
		}

		return new ForcedValue(builder.toString(), i, i - startIndex);
	}

	private record ForcedValue(String value, int endQuoteOffset, int rawLength)
	{
	}
}
