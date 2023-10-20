package org.logoce.lmf.model.resource.linking.tree;

import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.tree.NavigableTree;
import org.logoce.lmf.model.util.tree.StructuredTree;

public sealed interface LinkNode<I extends PNode> extends NavigableTree<LinkNode<I>>,
														  StructuredTree<LinkNode<I>> permits ErrorNode, ResolvedNode
{
	I pNode();
}
