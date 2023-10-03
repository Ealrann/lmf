package org.logoce.lmf.editor.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.LMFile;
import org.logoce.lmf.editor.LMLanguage;
import org.logoce.lmf.editor.lexer.LMEditorLexer;
import org.logoce.lmf.editor.lexer.LMEditorLexerAdapter;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;

public final class LMParserDefinition implements ParserDefinition
{
	public static final IFileElementType FILE = new IFileElementType(LMLanguage.INSTANCE);

	@NotNull
	@Override
	public Lexer createLexer(Project project)
	{
		return new LMEditorLexerAdapter(new LMEditorLexer(false));
	}

	@NotNull
	@Override
	public TokenSet getCommentTokens()
	{
		return TokenSet.WHITE_SPACE;
	}

	@NotNull
	@Override
	public TokenSet getStringLiteralElements()
	{
		return TokenSet.EMPTY;
	}

	@NotNull
	@Override
	public PsiParser createParser(final Project project)
	{
		return new LMParser();
	}

	@NotNull
	@Override
	public IFileElementType getFileNodeType()
	{
		return FILE;
	}

	@NotNull
	@Override
	public PsiFile createFile(@NotNull FileViewProvider viewProvider)
	{
		return new LMFile(viewProvider);
	}

	@NotNull
	@Override
	public PsiElement createElement(ASTNode node)
	{
		return LMIntellijTokenTypes.Factory.createElement(node);
	}

}