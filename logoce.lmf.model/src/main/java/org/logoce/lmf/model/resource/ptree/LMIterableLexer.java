package org.logoce.lmf.model.resource.ptree;

import org.logoce.lmf.model.lexer.ELMTokenType;
import org.logoce.lmf.model.lexer.LMLexer;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class LMIterableLexer implements Iterable<PToken>
{
	private final LMLexer lexer = new LMLexer(null);

	@Override
	public Iterator<PToken> iterator()
	{
		return new LexerIterator(lexer, Function.identity());
	}

	public void reset(final CharSequence text, final int initialState)
	{
		lexer.reset(text, 0, text.length(), initialState);
	}

	public Stream<PToken> stream()
	{
		return StreamSupport.stream(spliterator(), false);
	}

	private static final class LexerIterator implements Iterator<PToken>
	{
		private final LMLexer lmLexer;
		private final Function<ELMTokenType, ELMTokenType> mapper;
		private ELMTokenType current = null;

		private boolean progressed = false;

		public LexerIterator(final LMLexer lexer, final Function<ELMTokenType, ELMTokenType> mapper)
		{
			lmLexer = lexer;
			this.mapper = mapper;
		}

		@Override
		public boolean hasNext()
		{
			if (progressed == false) progress();
			progressed = true;
			return current != null;
		}

		@Override
		public PToken next()
		{
			if (progressed == false) progress();
			progressed = false;
			return new PToken(lmLexer.yytext().toString(), mapper.apply(current));
		}

		private void progress()
		{
			try
			{
				current = lmLexer.next();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
