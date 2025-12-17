package org.logoce.lmf.core.loader.api.text.syntax;

import org.logoce.lmf.core.loader.api.lexer.ELMTokenType;

public record PToken(String value, ELMTokenType type, int offset, int length)
{}
