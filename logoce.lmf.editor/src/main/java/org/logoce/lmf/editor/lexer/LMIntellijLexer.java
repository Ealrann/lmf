package org.logoce.lmf.editor.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;
import org.logoce.lmf.model.lexer.ELMTokenType;
import org.logoce.lmf.model.lexer.LMLexer;

import java.io.Reader;

public final class LMIntellijLexer extends LMLexer implements FlexLexer
{
	public LMIntellijLexer(final Reader in)
	{
		super(in);
	}

	@Override
	public IElementType advance() throws java.io.IOException
	{
		final var type = super.next();

		if (type == ELMTokenType.ASSIGN) return LMIntellijTokenTypes.ASSIGN;
		else if (type == ELMTokenType.BAD_CHARACTER) return LMIntellijTokenTypes.BAD_CHARACTER;
		else if (type == ELMTokenType.CLOSE_NODE) return LMIntellijTokenTypes.CLOSE_NODE;
		else if (type == ELMTokenType.LIST_SEPARATOR) return LMIntellijTokenTypes.LIST_SEPARATOR;
		else if (type == ELMTokenType.VALUE_NAME) return LMIntellijTokenTypes.VALUE_NAME;
		else if (type == ELMTokenType.TYPE_NAME) return LMIntellijTokenTypes.TYPE_NAME;
		else if (type == ELMTokenType.OPEN_NODE) return LMIntellijTokenTypes.OPEN_NODE;
		else if (type == ELMTokenType.QUOTE) return LMIntellijTokenTypes.QUOTE;
		else if (type == ELMTokenType.TYPE) return LMIntellijTokenTypes.TYPE;
		else if (type == ELMTokenType.VALUE) return LMIntellijTokenTypes.VALUE;
		else if (type == ELMTokenType.WHITE_SPACE) return LMIntellijTokenTypes.WHITE_SPACE;

		else if (type == null) return null;

		throw new IllegalArgumentException();
	}
}
