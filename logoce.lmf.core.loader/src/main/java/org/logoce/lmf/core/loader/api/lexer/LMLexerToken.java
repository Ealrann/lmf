package org.logoce.lmf.core.loader.api.lexer;

public interface LMLexerToken
{
	record SimpleLMLexerToken(String debugName) implements LMLexerToken {}
}
