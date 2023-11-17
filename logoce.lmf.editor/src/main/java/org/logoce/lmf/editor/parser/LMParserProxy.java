package org.logoce.lmf.editor.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;

public final class LMParserProxy implements PsiParser, LightPsiParser
{
	public static final TokenSet GROUP = TokenSet.create(LMIntellijTokenTypes.GROUP);

	public static final TokenSet GROUP_TYPE = TokenSet.create(LMIntellijTokenTypes.GROUP_TYPE);
	public static final TokenSet LEAF = TokenSet.create(LMIntellijTokenTypes.LEAF);
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

		/*final var trees = Arrays.stream(rootNode.getChildren(CHILDREN_SET))
								.map(PNodeView::of)
								.map(node -> new TreeView<>(node, PNodeView::children, PNodeView::parent))
								.toList();

		final var pModel = PMODEL_BUILDER.link(trees);
		for (final var tree : pModel.trees())
		{
			tree.forEach(LMParserProxy::pushResolutionList);
		}*/
		return rootNode;
	}

	@Override
	public void parseLight(final IElementType root, final PsiBuilder builder)
	{
		parser.parseLight(root, builder);
	}

	/*private static void pushResolutionList(final LinkNodeFull<PNodeView> linkNode)
	{
		final var pNode = linkNode.pNode().astGroup();
		if (linkNode.exception() != null)
		{
			assert pNode.getElementType() == LMIntellijTokenTypes.GROUP;
			pNode.putUserData(TOKEN_RESOLUTION, linkNode.exception());
		}
		else
		{
			pNode.putUserData(TOKEN_RESOLUTION, null);
		}
	}*/

}
