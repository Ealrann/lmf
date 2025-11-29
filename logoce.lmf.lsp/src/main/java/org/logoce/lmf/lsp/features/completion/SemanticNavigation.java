package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.Position;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.loader.linking.LinkNode;
import org.logoce.lmf.model.loader.linking.tree.LinkNodeFull;
import org.logoce.lmf.model.resource.parsing.PNode;

final class SemanticNavigation
{
	private SemanticNavigation()
	{
	}

	static Group<?> findGroupAtPosition(final SemanticSnapshot semantic,
										final SyntaxSnapshot syntax,
										final Position pos)
	{
		final PNode targetNode = SyntaxNavigation.findPNodeAtPosition(syntax, pos);
		if (targetNode == null)
		{
			return null;
		}

		for (final LinkNode<?, PNode> root : semantic.linkTrees())
		{
			final Group<?> group = findGroupInLinkTree(root, targetNode);
			if (group != null)
			{
				return group;
			}
		}

		return null;
	}

	static Group<?> findContainingGroupForNode(final SemanticSnapshot semantic,
											   final PNode target)
	{
		for (final LinkNode<?, PNode> root : semantic.linkTrees())
		{
			final Group<?> group = findContainingGroupInLinkTree(root, target);
			if (group != null)
			{
				return group;
			}
		}
		return null;
	}

	private static Group<?> findGroupInLinkTree(final LinkNode<?, PNode> node,
												final PNode target)
	{
		if (node.pNode() == target)
		{
			return node.group();
		}

		if (node instanceof LinkNodeFull<?, PNode> full)
		{
			for (final var child : full.streamChildren().toList())
			{
				final Group<?> g = findGroupInLinkTree(child, target);
				if (g != null)
				{
					return g;
				}
			}
		}

		return null;
	}

	private static Group<?> findContainingGroupInLinkTree(final LinkNode<?, PNode> node,
														  final PNode target)
	{
		if (node.pNode() == target)
		{
			return node.group();
		}

		if (node instanceof LinkNodeFull<?, PNode> full)
		{
			for (final var child : full.streamChildren().toList())
			{
				if (child.pNode() == target)
				{
					return node.group();
				}
				final Group<?> nested = findContainingGroupInLinkTree(child, target);
				if (nested != null)
				{
					return nested;
				}
			}
		}

		return null;
	}
}
