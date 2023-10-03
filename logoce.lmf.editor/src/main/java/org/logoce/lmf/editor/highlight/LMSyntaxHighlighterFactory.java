package org.logoce.lmf.editor.highlight;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.lexer.LMEditorLexer;

import java.util.HashMap;
import java.util.Map;

public final class LMSyntaxHighlighterFactory extends SyntaxHighlighterFactory
{
	public static final Key<Map<VirtualFile, LMEditorLexer>> LEXER_MAP_KEY = Key.create("LMLexer Map");

	@NotNull
	@Override
	public SyntaxHighlighter getSyntaxHighlighter(final Project project, final VirtualFile virtualFile)
	{
		final var lexer = getOrCreateLexer(project, virtualFile);
		return new LMSyntaxHighlighter(lexer);
	}

	private static LMEditorLexer getOrCreateLexer(final Project project, final VirtualFile virtualFile)
	{
		if (project != null)
		{
			final var map = getOrCreateMap(project);
			if (map.containsKey(virtualFile))
			{
				return map.get(virtualFile);
			}
			else
			{
				final var newLexer = new LMEditorLexer(true);
				map.put(virtualFile, newLexer);
				return newLexer;
			}
		}
		else
		{
			return new LMEditorLexer(false);
		}
	}

	public static LMEditorLexer getLexer(final Project project, final VirtualFile virtualFile)
	{
		assert project != null;
		final var map = getOrCreateMap(project);
		return map.getOrDefault(virtualFile, null);
	}

	private static Map<VirtualFile, LMEditorLexer> getOrCreateMap(final @NotNull Project project)
	{
		final var map = project.getUserData(LEXER_MAP_KEY);
		if (map == null)
		{
			final var newMap = new HashMap<VirtualFile, LMEditorLexer>();
			project.putUserData(LEXER_MAP_KEY, newMap);
			return newMap;
		}
		else
		{
			return map;
		}
	}
}
