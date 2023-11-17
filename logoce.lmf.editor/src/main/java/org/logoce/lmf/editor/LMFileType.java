package org.logoce.lmf.editor;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class LMFileType extends LanguageFileType
{
	public static final LMFileType INSTANCE = new LMFileType();

	private LMFileType()
	{
		super(LMLanguage.INSTANCE);
	}

	@NotNull
	@Override
	public String getName()
	{
		return "LightModel";
	}

	@NotNull
	@Override
	public String getDescription()
	{
		return "LMF_MODEL";
	}

	@NotNull
	@Override
	public String getDefaultExtension()
	{
		return "lm";
	}

	@Nullable
	@Override
	public Icon getIcon()
	{
		return LMIcons.ICON;
	}
}
