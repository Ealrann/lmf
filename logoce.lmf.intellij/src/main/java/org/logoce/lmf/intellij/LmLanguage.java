package org.logoce.lmf.intellij;

import com.intellij.lang.Language;

public final class LmLanguage extends Language
{
	private static final long serialVersionUID = 1L;

	public static final LmLanguage INSTANCE = new LmLanguage();

	private LmLanguage()
	{
		super("LMF");
	}
}
