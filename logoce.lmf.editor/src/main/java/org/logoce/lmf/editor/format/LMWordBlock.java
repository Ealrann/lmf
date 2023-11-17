package org.logoce.lmf.editor.format;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.formatter.common.AbstractBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class LMWordBlock extends AbstractBlock
{
	LMWordBlock(@NotNull final ASTNode node, @Nullable final Wrap wrap, @Nullable final Alignment alignment)
	{
		super(node, wrap, alignment);
	}

	@Override
	protected List<Block> buildChildren()
	{
		return List.of();
	}

	@Override
	public Indent getIndent()
	{
		return Indent.getNoneIndent();
	}

	@Nullable
	@Override
	public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2)
	{
		return Spacing.createSafeSpacing(false, 0);
	}

	@Override
	public boolean isLeaf()
	{
		return true;
	}
}
