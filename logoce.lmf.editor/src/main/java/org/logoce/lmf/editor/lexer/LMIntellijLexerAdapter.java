package org.logoce.lmf.editor.lexer;

import com.intellij.lexer.FlexAdapter;

public final class LMIntellijLexerAdapter extends FlexAdapter
{
	public LMIntellijLexerAdapter()
	{
		super(new LMIntellijLexer(null));
	}
}
