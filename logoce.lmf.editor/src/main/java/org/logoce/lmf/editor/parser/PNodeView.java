package org.logoce.lmf.editor.parser;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.logoce.lmf.editor.lexer.LMEditorLexer;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PToken;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public record PNodeView(ASTNode astGroup, List<PToken> tokens) implements PNode
{
	private static final TokenSet CHILDREN_SET = TokenSet.create(LMIntellijTokenTypes.GROUP);

	public static PNodeView of(ASTNode group)
	{
		final var pTokens = Arrays.stream(group.getChildren(null))
								  .mapMulti(PNodeView::triageTokens)
								  .map(PNodeView::buildPNode)
								  .toList();
		return new PNodeView(group, pTokens);
	}

	public Stream<PNodeView> children()
	{
		return Arrays.stream(astGroup.getChildren(CHILDREN_SET)).map(PNodeView::of);
	}

	public PNodeView parent()
	{
		final var treeParent = astGroup.getTreeParent();
		if (treeParent.getElementType() instanceof IFileElementType)
		{
			return null;
		}
		else
		{
			return PNodeView.of(treeParent);
		}
	}

	private static void triageTokens(ASTNode node, Consumer<ASTNode> consumer)
	{
		final var elementType = node.getElementType();
		if (elementType == LMIntellijTokenTypes.VAL ||
			elementType == LMIntellijTokenTypes.GROUP_TYPE ||
			elementType == LMIntellijTokenTypes.LEAF)
		{
			for (final var child : node.getChildren(null))
			{
				triageTokens(child, consumer);
			}
		}
		else if (elementType != LMIntellijTokenTypes.OPEN_NODE &&
				 elementType != LMIntellijTokenTypes.CLOSE_NODE &&
				 elementType != LMIntellijTokenTypes.GROUP)
		{
			consumer.accept(node);
		}
	}

	private static PToken buildPNode(final ASTNode token)
	{
		final var text = token.getText();
		final var type = LMEditorLexer.unwrapType(token.getElementType());
		return new PToken(text, type);
	}
}
