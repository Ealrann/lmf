package org.logoce.lmf.editor.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.util.Key;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.lexer.LMEditorLexer;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;
import org.logoce.lmf.model.resource.linking.exception.LinkException;
import org.logoce.lmf.model.resource.linking.tree.ErrorNode;
import org.logoce.lmf.model.resource.linking.tree.LinkNode;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.resource.transform.PModelBuilder;
import org.logoce.lmf.model.util.tree.TreeView;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class LMParserProxy implements PsiParser, LightPsiParser
{
	public static final Key<LinkException> TOKEN_RESOLUTION = Key.create("TOKEN_RESOLUTION");

	private static final PModelBuilder<PNodeView> PMODEL_BUILDER = new PModelBuilder<>((a, b) -> {});
	private static final TokenSet CHILDREN_SET = TokenSet.create(LMIntellijTokenTypes.GROUP);

	private static final TokenSet GROUP_TYPE = TokenSet.create(LMIntellijTokenTypes.GROUP_TYPE);
	private static final TokenSet LEAF = TokenSet.create(LMIntellijTokenTypes.LEAF);
	private static final TokenSet VAL = TokenSet.create(LMIntellijTokenTypes.VAL);

	private static final TokenSet OPEN_NODE = TokenSet.create(LMIntellijTokenTypes.OPEN_NODE);
	private static final TokenSet CLOSE_NODE = TokenSet.create(LMIntellijTokenTypes.CLOSE_NODE);
	private static final TokenSet NOT_OPEN_CLOSE = TokenSet.orSet(OPEN_NODE, CLOSE_NODE);

	private static final TokenSet TOKEN_SET = TokenSet.orSet(GROUP_TYPE, LEAF, VAL);

	private final LMParser parser = new LMParser();

	@Override
	public @NotNull ASTNode parse(@NotNull final IElementType root, @NotNull final PsiBuilder builder)
	{
		final var rootNode = parser.parse(root, builder);

		final var trees = Arrays.stream(rootNode.getChildren(CHILDREN_SET))
								.map(PNodeView::of)
								.map(node -> new TreeView<>(node, PNodeView::children))
								.toList();

		final var pModel = PMODEL_BUILDER.link(trees);
		for (final var tree : pModel.trees())
		{
			tree.forEach(LMParserProxy::pushResolutionList);
		}
		return rootNode;
	}

	@Override
	public void parseLight(final IElementType root, final PsiBuilder builder)
	{
		parser.parseLight(root, builder);
	}

	private static void pushResolutionList(final LinkNode<PNodeView> linkNode)
	{
		final var pNode = linkNode.pNode().astGroup();
		if (linkNode instanceof ErrorNode<PNodeView> errorNode)
		{
			assert pNode.getElementType() == LMIntellijTokenTypes.GROUP;
			pNode.putUserData(TOKEN_RESOLUTION, errorNode.getException());
		}
		else
		{
			pNode.putUserData(TOKEN_RESOLUTION, null);
		}
	}

	private record PNodeView(ASTNode astGroup, List<PToken> tokens) implements PNode
	{
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
}
