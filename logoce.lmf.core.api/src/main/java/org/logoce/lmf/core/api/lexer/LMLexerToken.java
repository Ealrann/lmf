package org.logoce.lmf.core.api.lexer;

public interface LMLexerToken
{
	record SimpleLMLexerToken(String debugName) implements LMLexerToken {}
}
