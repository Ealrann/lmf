package org.logoce.lmf.editor.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.lexer.ModelLexer;

import java.util.HashMap;
import java.util.Map;

public final class ModelLexerUtil
{
	public static final Key<Map<VirtualFile, ModelLexer>> LEXER_MAP_KEY = Key.create("LMLexer Map");

	public static ModelLexer getOrCreateLexer(final Project project, final VirtualFile virtualFile)
	{
		assert project != null;

		final var map = getOrCreateMap(project);
		if (map.containsKey(virtualFile))
		{
			return map.get(virtualFile);
		}
		else
		{
			final var newLexer = new ModelLexer();
			map.put(virtualFile, newLexer);
			return newLexer;
		}
	}

	public static ModelLexer getLexer(final Project project, final VirtualFile virtualFile)
	{
		assert project != null;
		final var map = getOrCreateMap(project);
		return map.getOrDefault(virtualFile, null);
	}

	private static Map<VirtualFile, ModelLexer> getOrCreateMap(final @NotNull Project project)
	{
		final var map = project.getUserData(LEXER_MAP_KEY);
		if (map == null)
		{
			final var newMap = new HashMap<VirtualFile, ModelLexer>();
			project.putUserData(LEXER_MAP_KEY, newMap);
			return newMap;
		}
		else
		{
			return map;
		}
	}
}
