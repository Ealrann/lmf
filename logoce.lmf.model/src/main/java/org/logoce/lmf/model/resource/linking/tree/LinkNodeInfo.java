package org.logoce.lmf.model.resource.linking.tree;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.linking.ModelGroup;
import org.logoce.lmf.model.resource.linking.exception.LinkException;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.tree.NavigableDataTree;

import java.util.List;

public sealed interface LinkNodeInfo<I extends PNode> permits LinkNodeInfo.Resolved, LinkNodeInfo.Failed
{
	I pNode();

	LinkNode<I> buildNode(NavigableDataTree.BuildInfo<LinkNodeInfo<I>, LinkNode<I>> buildInfo);

	record Resolved<T extends LMObject, I extends PNode>(I pNode,
														 Relation<T, ?> containingRelation,
														 List<PFeature> features,
														 ModelGroup<T> modelGroup) implements LinkNodeInfo<I>
	{
		@Override
		public LinkNode<I> buildNode(final NavigableDataTree.BuildInfo<LinkNodeInfo<I>, LinkNode<I>> buildInfo)
		{
			final var data = (LinkNodeInfo.Resolved<?, I>) buildInfo.data();
			return new ResolvedNode<>(buildInfo.parent(), data, buildInfo.childrenBuilder());
		}
	}

	record Failed<I extends PNode>(I pNode, LinkException exception) implements LinkNodeInfo<I>
	{
		@Override
		public LinkNode<I> buildNode(final NavigableDataTree.BuildInfo<LinkNodeInfo<I>, LinkNode<I>> buildInfo)
		{
			final var error = (LinkNodeInfo.Failed<I>) buildInfo.data();
			return new ErrorNode<>(error.pNode(), buildInfo.parent(), error.exception());
		}
	}
}
