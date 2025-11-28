package org.logoce.lmf.intellij;

import com.intellij.openapi.fileTypes.LanguageFileType;

import javax.swing.Icon;

public final class LmFileType extends LanguageFileType
{
	public static final LmFileType INSTANCE = new LmFileType();

	private LmFileType()
	{
		super(LmLanguage.INSTANCE);
	}

	@Override
	public String getName()
	{
		return "LMF";
	}

	@Override
	public String getDescription()
	{
		return "LMF meta-model file";
	}

	@Override
	public String getDefaultExtension()
	{
		return "lm";
	}

	@Override
	public Icon getIcon()
	{
		return null;
	}
}

