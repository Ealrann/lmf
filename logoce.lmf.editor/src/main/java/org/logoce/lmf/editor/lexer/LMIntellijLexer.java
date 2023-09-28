package org.logoce.lmf.editor.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.logoce.lmf.model.lexer.LMTokenTypes;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;
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

		if (type == LMTokenTypes.ELEMENT) return LMIntellijTokenTypes.ELEMENT;
		else if (type == LMTokenTypes.LIST) return LMIntellijTokenTypes.LIST;
		else if (type == LMTokenTypes.NAMED) return LMIntellijTokenTypes.NAMED;
		else if (type == LMTokenTypes.VAL) return LMIntellijTokenTypes.VAL;
		else if (type == LMTokenTypes.WORD) return LMIntellijTokenTypes.WORD;
		else if (type == LMTokenTypes.ASSIGN) return LMIntellijTokenTypes.ASSIGN;
		else if (type == LMTokenTypes.BAD_CHARACTER) return LMIntellijTokenTypes.BAD_CHARACTER;
		else if (type == LMTokenTypes.CLOSE_NODE) return LMIntellijTokenTypes.CLOSE_NODE;
		else if (type == LMTokenTypes.LIST_SEPARATOR) return LMIntellijTokenTypes.LIST_SEPARATOR;
		else if (type == LMTokenTypes.NAME) return LMIntellijTokenTypes.NAME;
		else if (type == LMTokenTypes.OPEN_NODE) return LMIntellijTokenTypes.OPEN_NODE;
		else if (type == LMTokenTypes.QUOTE) return LMIntellijTokenTypes.QUOTE;
		else if (type == LMTokenTypes.TYPE) return LMIntellijTokenTypes.TYPE;
		else if (type == LMTokenTypes.VALUE) return LMIntellijTokenTypes.VALUE;
		else if (type == LMTokenTypes.WHITE_SPACE) return LMIntellijTokenTypes.WHITE_SPACE;

		else if (type == null) return null;

		throw new IllegalArgumentException();
	}
}
