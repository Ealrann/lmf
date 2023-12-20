package org.logoce.lmf.editor.ref;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.logoce.lmf.editor.parser.PNodeView;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.resource.linking.exception.LinkException;
import org.logoce.lmf.model.resource.linking.feature.AttributeResolver;
import org.logoce.lmf.model.resource.transform.LinkNode;
import org.logoce.lmf.model.resource.transform.PModelLinker;
import org.logoce.lmf.model.resource.transform.ResolutionAttempt;
import org.logoce.lmf.model.util.ModelUtils;
import org.logoce.lmf.model.util.tree.TreeView;

import java.util.function.BiConsumer;

public class LMNamedElementImpl extends ASTWrapperPsiElement implements LMNamedElement
{
	private final static PModelLinker<PNodeView> PMDEL_BUILDER = new PModelLinker<>();

	public LMNamedElementImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public @Nullable PsiElement getNameIdentifier()
	{
		return null;
	}

	@Override
	public PsiElement setName(@NotNull final String name) throws IncorrectOperationException
	{
		return null;
	}

	public String resolvedName()
	{
		final var node = linkPartial((a, e) -> {});
		if (ModelUtils.isSubGroup(node.group(), LMCoreDefinition.Groups.NAMED))
		{
			final var nameResolution = node.attributeResolutions()
										   .stream()
										   .map(ResolutionAttempt::resolution)
										   .filter(f -> f.feature() == LMCoreDefinition.Features.NAMED.NAME)
										   .map(r -> (AttributeResolver.AttributeResolution<?>) r)
										   .findAny();

			return nameResolution.map(AttributeResolver.AttributeResolution::value).orElse(null);
		}
		else
		{
			return null;
		}
	}

	public LinkNode<?, PNodeView> linkPartial(final BiConsumer<LinkException, PNodeView> exceptionConsumer)
	{
		return PMDEL_BUILDER.linkPartial(treeView(), exceptionConsumer);
	}

	public TreeView<PNodeView> treeView()
	{
		final var astGroup = getNode();
		final var groupNode = PNodeView.of(astGroup);
		return new TreeView<>(groupNode, PNodeView::children, PNodeView::parent);
	}
}
