package org.logoce.lmf.editor.format;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.formatter.common.AbstractBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;
import org.logoce.lmf.editor.util.ASTChildrenIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LMGroupBlock extends AbstractBlock
{
	private final SpacingBuilder spacingBuilder;
	private final boolean root;

	LMGroupBlock(@NotNull final ASTNode node,
				 @Nullable final Wrap wrap,
				 @Nullable final Alignment alignment,
				 final SpacingBuilder spacingBuilder,
				 final boolean root)
	{
		super(node, wrap, alignment);
		this.spacingBuilder = spacingBuilder;
		this.root = root;
	}

	@Override
	protected List<Block> buildChildren()
	{
		List<Block> blocks = new ArrayList<>();

		var child = myNode.getFirstChildNode();

		while (child != null)
		{
			if (child.getElementType() != LMIntellijTokenTypes.WHITE_SPACE)
			{
				if (child.getElementType() == LMIntellijTokenTypes.GROUP)
				{
					final var block = createGroupBlock(child);
					blocks.add(block);
				}
				else
				{
					final var block = createLeafBlock(child);
					blocks.add(block);
				}
			}

			child = child.getTreeNext();
		}

		return blocks;


		// final var childrenView = new ASTChildrenIterable(myNode);

		/*return childrenView.streamChildren()
						   .map(this::buildBlock)
						   .filter(Optional::isPresent)
						   .map(Optional::get)
						   .toList();*/
	}

	private Optional<Block> buildBlock(final ASTNode child)
	{
		if (child.getElementType() != LMIntellijTokenTypes.WHITE_SPACE)
		{
			if (child.getElementType() == LMIntellijTokenTypes.GROUP)
			{
				return Optional.of(createGroupBlock(child));
			}
			else
			{
				return Optional.of(createLeafBlock(child));
			}
		}
		else
		{
			return Optional.empty();
		}
	}

	@NotNull
	private static LMWordBlock createLeafBlock(final ASTNode child)
	{
		return new LMWordBlock(child, Wrap.createWrap(WrapType.NONE, false), Alignment.createAlignment(true));
	}

	@NotNull
	private LMGroupBlock createGroupBlock(final ASTNode child)
	{
		return new LMGroupBlock(child,
								Wrap.createWrap(WrapType.ALWAYS, true),
								Alignment.createAlignment(true),
								spacingBuilder,
								false);
	}

	@Override
	public Indent getIndent()
	{
		if (root) return Indent.getNoneIndent();
		else return Indent.getSpaceIndent(4);
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
