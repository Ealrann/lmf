package org.logoce.lmf.editor.psi;

import com.intellij.lang.ASTNode;

public class LMPsiImplUtil
{
	public static String getKey(LMFLeaf element)
	{
		final ASTNode keyNode = element.getNode().findChildByType(LMIntellijTokenTypes.GROUP);
		if (keyNode != null)
		{
			// IMPORTANT: Convert embedded escaped spaces to simple spaces
			return keyNode.getText().replaceAll("\\\\ ", " ");
		}
		else
		{
			return null;
		}
	}

	public static String getValue(LMFLeaf element)
	{
		final ASTNode valueNode = element.getNode().findChildByType(LMIntellijTokenTypes.GROUP);
		if (valueNode != null)
		{
			return valueNode.getText();
		}
		else
		{
			return null;
		}
	}
}
