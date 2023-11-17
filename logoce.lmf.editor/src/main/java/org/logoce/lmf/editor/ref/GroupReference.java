package org.logoce.lmf.editor.ref;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.logoce.lmf.editor.LMIcons;
import org.logoce.lmf.editor.psi.LMFGroup;
import org.logoce.lmf.editor.util.LMUtil;

import java.util.ArrayList;
import java.util.List;

public final class GroupReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference
{

	private final String key;

	public GroupReference(@NotNull PsiElement element, TextRange textRange)
	{
		super(element, textRange);
		key = element.getText().substring(textRange.getStartOffset(), textRange.getEndOffset());
	}

	@Override
	public ResolveResult @NotNull [] multiResolve(boolean incompleteCode)
	{
		final var project = myElement.getProject();
		final List<LMFGroup> groups = LMUtil.findGroups(project, key);
		final List<ResolveResult> results = new ArrayList<>();
		for (LMFGroup group : groups)
		{
			results.add(new PsiElementResolveResult(group));
		}
		return results.toArray(new ResolveResult[0]);
	}

	@Nullable
	@Override
	public PsiElement resolve()
	{
		final ResolveResult[] resolveResults = multiResolve(false);
		return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
	}

	@Override
	public Object @NotNull [] getVariants()
	{
		final var project = myElement.getProject();
		List<LookupElement> variants = new ArrayList<>();
		/*List<LMFGroup> groups = LMUtil.findGroups(project);
		for (final LMFGroup group : groups)
		{
			if (group.getKey() != null && group.getKey().length() > 0)
			{
				variants.add(LookupElementBuilder.create(group)
												 .withIcon(LMIcons.ICON)
												 .withTypeText(group.getContainingFile().getName()));
			}
		}*/
		return variants.toArray();
	}

}