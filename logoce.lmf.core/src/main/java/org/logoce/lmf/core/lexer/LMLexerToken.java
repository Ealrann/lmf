package org.logoce.lmf.core.lexer;

public interface LMLexerToken
{
	record SimpleLMLexerToken(String debugName) implements LMLexerToken {}
}
