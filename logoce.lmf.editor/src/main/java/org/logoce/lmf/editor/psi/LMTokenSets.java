package org.logoce.lmf.editor.psi;

import com.intellij.psi.tree.TokenSet;

public class LMTokenSets
{
	public static final LMTokenType IDENTIFIER = new LMTokenType("Identifier");
	public static final TokenSet COMMENTS = TokenSet.create(new LMTokenType("Comments"));
}
