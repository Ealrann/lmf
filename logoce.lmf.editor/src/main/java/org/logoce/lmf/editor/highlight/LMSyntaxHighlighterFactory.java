package org.logoce.lmf.editor.highlight;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public final class LMSyntaxHighlighterFactory extends SyntaxHighlighterFactory
{
	@NotNull
	@Override
	public SyntaxHighlighter getSyntaxHighlighter(final Project project, final VirtualFile virtualFile)
	{
		return new LMSyntaxHighlighter();
	}
}
