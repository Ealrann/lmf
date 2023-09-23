package org.logoce.lmf.editor.lexer;

import com.intellij.lexer.FlexAdapter;

public class LMLexerAdapter extends FlexAdapter
{

	public LMLexerAdapter()
	{
		super(new LMLexer(null));
	}

}