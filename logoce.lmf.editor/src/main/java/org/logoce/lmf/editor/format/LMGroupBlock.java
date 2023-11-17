package org.logoce.lmf.editor.format;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LMGroupBlock extends AbstractBlock
{
	public static final @NotNull TokenSet GROUP_TOKEN_SET = TokenSet.create(LMIntellijTokenTypes.GROUP);

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
		final var childGroupCount = myNode.getChildren(GROUP_TOKEN_SET).length;
		final var blockBuilder = new BlockBuilder(spacingBuilder, childGroupCount > 1);
		final List<Block> blocks = new ArrayList<>();

		var child = myNode.getFirstChildNode();

		while (child != null)
		{
			if (child.getElementType() != LMIntellijTokenTypes.WHITE_SPACE)
			{
				if (child.getElementType() == LMIntellijTokenTypes.GROUP)
				{
					final var block = blockBuilder.createGroupBlock(child);
					blocks.add(block);
				}
				else
				{
					final var block = BlockBuilder.createLeafBlock(child);
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

	private static final class BlockBuilder
	{
		private final SpacingBuilder spacingBuilder;
		private final boolean wrapGroups;

		public BlockBuilder(final SpacingBuilder spacingBuilder, final boolean wrapGroups)
		{
			this.spacingBuilder = spacingBuilder;
			this.wrapGroups = wrapGroups;
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
			final var wrapType = wrapGroups
								 ? Wrap.createWrap(WrapType.ALWAYS, true)
								 : Wrap.createWrap(WrapType.NONE, false);

			return new LMGroupBlock(child, wrapType, Alignment.createAlignment(true), spacingBuilder, false);
		}
	}
}
