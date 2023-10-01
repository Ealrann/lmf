package org.logoce.lmf.model.resource.ptree;

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
			return token == null ? Optional.empty() : Optional.of(new PToken(lexer.yytext().toString(), token));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void reset(final CharSequence text, final int initialState)
	{
		lexer.reset(text, 0, text.length(), initialState);
	}

	public Stream<PToken> stream()
	{
		return StreamSupport.stream(spliterator(), false);
	}
}
