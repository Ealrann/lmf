package org.logoce.lmf.editor;

import com.intellij.lang.Language;

public final class LMLanguage extends Language
{
	public static final LMLanguage INSTANCE = new LMLanguage();

	private LMLanguage()
	{
		super("LightModel");
	}
}
