package org.logoce.lmf.editor.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.logoce.lmf.editor.LMFile;
import org.logoce.lmf.editor.LMFileType;
import org.logoce.lmf.editor.psi.LMFGroup;

import java.util.ArrayList;
import java.util.List;

public final class LMUtil
{
	public static List<LMFGroup> findGroups(final Project project,final String key)
	{
		final List<LMFGroup> result = new ArrayList<>();
		final var virtualFiles = FileTypeIndex.getFiles(LMFileType.INSTANCE, GlobalSearchScope.allScope(project));
		for (VirtualFile virtualFile : virtualFiles)
		{
			final var lmFile = (LMFile) PsiManager.getInstance(project).findFile(virtualFile);
			if (lmFile != null)
			{
				final var groups = PsiTreeUtil.getChildrenOfType(lmFile, LMFGroup.class);
				if (groups != null)
				{
					for (final var group : groups)
					{
						if (key.equals(group.getText()))
						{
							result.add(group);
						}
					}
				}
			}
		}
		return result;
	}
}
