package org.logoce.lmf.editorfx.lang;

public final class Token {
	private final TokenType type;
	private final int start;
	private final int end;

	public Token(TokenType type, int start, int end) {
		this.type = type;
		this.start = start;
		this.end = end;
	}

	public TokenType type() {
		return type;
	}

	public int start() {
		return start;
	}

	public int end() {
		return end;
	}
}
