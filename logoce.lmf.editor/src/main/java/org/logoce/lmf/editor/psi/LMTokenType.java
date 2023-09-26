package org.logoce.lmf.editor.psi;

import com.intellij.psi.tree.IElementType;
import org.logoce.lmf.editor.LMLanguage;

public final class LMTokenType extends IElementType
{
	public LMTokenType(String debugName)
	{
		super(debugName, LMLanguage.INSTANCE);
	}
}
