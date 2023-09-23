package org.logoce.lmf.editor.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.LMLanguage;

public class LMElementType extends IElementType
{
	public LMElementType(@NonNls @NotNull final String debugName)
	{
		super(debugName, LMLanguage.INSTANCE);
	}
}
