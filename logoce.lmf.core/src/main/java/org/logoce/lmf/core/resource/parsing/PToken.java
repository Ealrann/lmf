package org.logoce.lmf.core.resource.parsing;

import org.logoce.lmf.core.lexer.ELMTokenType;

public record PToken(String value, ELMTokenType type, int offset, int length)
{}
