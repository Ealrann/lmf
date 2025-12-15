package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.Position;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.core.loader.linking.LinkNode;
import org.logoce.lmf.core.api.text.syntax.PNode;

final class LinkTreeNavigation
{
	private LinkTreeNavigation()
	{
	}

	static LinkNode<?, PNode> findLinkNodeAtOrBeforePosition(final SemanticSnapshot semantic,
															 final SyntaxSnapshot syntax,
															 final Position pos)
	{
		if (semantic == null || syntax == null)
		{
			return null;
		}

		final PNode headerNode = SyntaxNavigation.findPNodeAtOrBeforePosition(syntax, pos);
		if (headerNode == null)
		{
			return null;
		}

		return SemanticNavigation.findLinkNodeForNode(semantic, headerNode);
	}
}

