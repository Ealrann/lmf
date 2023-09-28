package org.logoce.lmf.editor.lexer;

import com.intellij.lexer.FlexAdapter;

public final class LMLexerAdapter extends FlexAdapter
{
	public LMLexerAdapter()
	{
		super(new LMIntellijLexer(null));
	}
}
