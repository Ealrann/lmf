package org.logoce.lmf.editor.format;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.formatter.common.AbstractBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;

import java.util.ArrayList;
import java.util.List;

public final class LMFileBlock extends AbstractBlock
{
	private final SpacingBuilder spacingBuilder;

	LMFileBlock(@NotNull final ASTNode node,
				@Nullable final Wrap wrap,
				@Nullable final Alignment alignment,
				final SpacingBuilder spacingBuilder)
	{
		super(node, wrap, alignment);
		this.spacingBuilder = spacingBuilder;
	}

	@Override
	protected List<Block> buildChildren()
	{
		final List<Block> blocks = new ArrayList<>();
		ASTNode child = myNode.getFirstChildNode();
		while (child != null)
		{
			if (child.getElementType() != LMIntellijTokenTypes.WHITE_SPACE)
			{
				final Block block = new LMGroupBlock(child,
													 Wrap.createWrap(WrapType.ALWAYS, true),
													 Alignment.createAlignment(false),
													 spacingBuilder,
													 true);
				blocks.add(block);
			}
			child = child.getTreeNext();
		}
		return blocks;
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
		return spacingBuilder.getSpacing(this, child1, child2);
	}

	@Override
	public boolean isLeaf()
	{
		return myNode.getFirstChildNode() == null;
	}
}
