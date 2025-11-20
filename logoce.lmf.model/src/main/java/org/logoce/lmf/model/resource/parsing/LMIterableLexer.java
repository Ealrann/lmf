package org.logoce.lmf.model.resource.parsing;

import org.logoce.lmf.model.lexer.LMLexer;
import org.logoce.lmf.model.resource.util.SoftIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class LMIterableLexer implements Iterable<PToken>
{
	private final LMLexer lexer = new LMLexer(null);
	private int offset = 0;
	private int lastLength = 0;

	@Override
	public Iterator<PToken> iterator()
	{
		return new SoftIterator<>(this::nextToken);
	}

	private Optional<PToken> nextToken()
	{
		try
		{
			final var token = lexer.next();
			if (token == null) return Optional.empty();
			final String text = lexer.yytext().toString();
			final int start = offset;
			lastLength = text.length();
			offset += lastLength;
			return Optional.of(new PToken(text, token, start, text.length()));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void reset(final CharSequence text, final int initialState)
	{
		lexer.reset(text, 0, text.length(), initialState);
		offset = 0;
		lastLength = 0;
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
}
