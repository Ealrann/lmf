package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.Position;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.loader.api.loader.linking.LinkNode;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeFull;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SemanticNavigation
{
	private static final Logger LOG = LoggerFactory.getLogger(SemanticNavigation.class);

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

	static LinkNode<?, PNode> findLinkNodeForNode(final SemanticSnapshot semantic,
												  final PNode target)
	{
		for (final LinkNode<?, PNode> root : semantic.linkTrees())
		{
			final LinkNode<?, PNode> found = findLinkNodeInTree(root, target);
			if (found != null)
			{
				return found;
			}
		}
		return null;
	}

	static Feature<?, ?, ?, ?> findFeatureAtValuePosition(final SemanticSnapshot semantic,
														  final SyntaxSnapshot syntax,
														  final Position pos)
	{
		final PNode headerNode = SyntaxNavigation.findPNodeAtOrBeforePosition(syntax, pos);
		if (headerNode == null)
		{
			LOG.debug("LMF LSP completion: SemanticNavigation.findFeatureAtValuePosition – no PNode at or before position line={}, character={}",
					  pos.getLine(), pos.getCharacter());
			return null;
		}

		final LinkNode<?, PNode> linkNode = findLinkNodeForNode(semantic, headerNode);
		if (linkNode == null)
		{
			final String keyword = SyntaxNavigation.headerKeyword(headerNode);
			final String name = SyntaxNavigation.headerName(headerNode);
			LOG.debug("LMF LSP completion: SemanticNavigation.findFeatureAtValuePosition – no link node for header keyword={}, name={}, line={}, character={}",
					  keyword, name, pos.getLine(), pos.getCharacter());
			return null;
		}

		final Group<?> group = linkNode.group();
		if (group == null)
		{
			LOG.debug("LMF LSP completion: SemanticNavigation.findFeatureAtValuePosition – link node has null group at line={}, character={}",
					  pos.getLine(), pos.getCharacter());
			return null;
		}

		final var headerInfo = CompletionContextResolver.HeaderInfo.from(syntax, pos);
		final String featureName = headerInfo != null ? headerInfo.featureName() : null;
		if (featureName == null || featureName.isBlank())
		{
			LOG.debug("LMF LSP completion: SemanticNavigation.findFeatureAtValuePosition – no feature name at line={}, character={}, group={}",
					  pos.getLine(), pos.getCharacter(), group.name());
			return null;
		}

		for (final Feature<?, ?, ?, ?> feature : group.features())
		{
			if (featureName.equals(feature.name()))
			{
				LOG.debug("LMF LSP completion: SemanticNavigation.findFeatureAtValuePosition – resolved feature={} on group={} at line={}, character={}",
						  featureName, group.name(), pos.getLine(), pos.getCharacter());
				return feature;
			}
		}

		LOG.debug("LMF LSP completion: SemanticNavigation.findFeatureAtValuePosition – feature '{}' not found on group={} at line={}, character={}",
				  featureName, group.name(), pos.getLine(), pos.getCharacter());
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

	private static LinkNode<?, PNode> findLinkNodeInTree(final LinkNode<?, PNode> node,
														 final PNode target)
	{
		if (node.pNode() == target)
		{
			return node;
		}

		if (node instanceof LinkNodeFull<?, PNode> full)
		{
			for (final var child : full.streamChildren().toList())
			{
				final LinkNode<?, PNode> nested = findLinkNodeInTree(child, target);
				if (nested != null)
				{
					return nested;
				}
			}
		}

		return null;
	}
}
