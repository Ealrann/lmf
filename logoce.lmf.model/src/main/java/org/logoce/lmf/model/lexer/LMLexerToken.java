package org.logoce.lmf.model.lexer;

public interface LMLexerToken
{
	record SimpleLMLexerToken(String debugName) implements LMLexerToken {}
}
