package org.logoce.lmf.core.api.text.syntax;

import org.logoce.lmf.core.api.lexer.ELMTokenType;

public record PToken(String value, ELMTokenType type, int offset, int length)
{}
