package org.logoce.lmf.editorfx.lang;

import org.logoce.lmf.model.lexer.ELMTokenType;
import org.logoce.lmf.model.resource.parsing.LMIterableLexer;
import org.logoce.lmf.model.resource.parsing.PToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Adapts the generated LMCore lexer into editor-friendly tokens with offsets.
 */
public final class LMCoreLexerAdapter {
	private static final Set<String> KEYWORDS = Set.of(
		"MetaModel", "Group", "Definition", "Enum", "Unit", "includes",
		"+att", "-att", "+contains", "-contains", "+refers", "-refers",
		"Generic", "generics", "name", "domain", "datatype", "reference", "parameters"
	);

	public List<Token> lex(String input) {
		final LMIterableLexer lexer = new LMIterableLexer();
		lexer.reset(input, 0);

		final List<Token> tokens = new ArrayList<>();
		for (PToken p : lexer) {
			final String value = p.value();
			final TokenType type = mapType(p.type(), value);
			final int start = p.offset();
			final int end = start + p.length();
			tokens.add(new Token(type, start, end));
		}
		return tokens;
	}

	private TokenType mapType(ELMTokenType elm, String value) {
		return switch (elm) {
			case OPEN_NODE -> TokenType.LPAREN;
			case CLOSE_NODE -> TokenType.RPAREN;
			case ASSIGN -> TokenType.ASSIGN;
			case QUOTE -> TokenType.STRING;
			case LIST_SEPARATOR -> TokenType.SYMBOL;
			case TYPE, TYPE_NAME, VALUE_NAME -> KEYWORDS.contains(value) ? TokenType.KEYWORD : TokenType.IDENT;
			case VALUE -> classifyValue(value);
			case WHITE_SPACE -> TokenType.WHITESPACE;
			case BAD_CHARACTER -> TokenType.OTHER;
		};
	}

	private TokenType classifyValue(String value) {
		if (value.startsWith("\"") && value.endsWith("\"")) {
			return TokenType.STRING;
		}
		if (value.matches("^-?\\d+(\\.\\d+)?$")) {
			return TokenType.NUMBER;
		}
		return KEYWORDS.contains(value) ? TokenType.KEYWORD : TokenType.IDENT;
	}
}
