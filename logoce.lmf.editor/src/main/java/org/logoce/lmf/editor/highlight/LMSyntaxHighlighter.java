package org.logoce.lmf.editor.highlight;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.lexer.LMEditorLexer;
import org.logoce.lmf.editor.lexer.LMEditorLexerAdapter;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public final class LMSyntaxHighlighter extends SyntaxHighlighterBase
{
	public static final TextAttributesKey PARENTHESES = createTextAttributesKey("LM_SEPARATOR",
																				DefaultLanguageHighlighterColors.PARENTHESES);
	public static final TextAttributesKey VALUE = createTextAttributesKey("LM_VALUE",
																		  DefaultLanguageHighlighterColors.STRING);
	public static final TextAttributesKey KEY = createTextAttributesKey("LM_KEY",
																		DefaultLanguageHighlighterColors.KEYWORD);

	public static final TextAttributesKey REFERENCE = createTextAttributesKey("LM_REFERENCE",
																			  DefaultLanguageHighlighterColors.INSTANCE_FIELD);

	private static final TextAttributesKey[] PARENTHESES_KEYS = new TextAttributesKey[]{PARENTHESES};
	private static final TextAttributesKey[] VALUE_KEYS = new TextAttributesKey[]{VALUE};
	private static final TextAttributesKey[] KEY_KEYS = new TextAttributesKey[]{KEY};
	private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

	private final LMEditorLexerAdapter lexerAdapter;

	public LMSyntaxHighlighter()
	{
		this.lexerAdapter = new LMEditorLexerAdapter(new LMEditorLexer());
	}

	@NotNull
	@Override
	public Lexer getHighlightingLexer()
	{
		return lexerAdapter;
	}

	@Override
	public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType)
	{
		if (tokenType == LMIntellijTokenTypes.OPEN_NODE || tokenType == LMIntellijTokenTypes.CLOSE_NODE)
		{
			return PARENTHESES_KEYS;
		}
		else if (tokenType == LMIntellijTokenTypes.TYPE)
		{
			return KEY_KEYS;
		}
		else if (tokenType == LMIntellijTokenTypes.VALUE)
		{
			return VALUE_KEYS;
		}
		return EMPTY_KEYS;
	}
}
