package org.logoce.lmf.model.resource.parsing;

import org.logoce.lmf.model.lexer.ELMTokenType;

public record PToken(String value, ELMTokenType type, int offset, int length)
{}
