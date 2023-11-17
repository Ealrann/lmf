package org.logoce.lmf.model.resource.linking.tree;

import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.linking.feature.NodeLinker;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.transform.LinkNode;

import java.util.List;
import java.util.stream.Stream;

public interface LinkNodeInternal<T extends LMObject, I extends PNode> extends LinkNode<T, I>
{
	List<PFeature> features();
	void resolveTokens(final NodeLinker nodeLinker);

	@Override
	LinkNodeInternal<?, I> parent();
	@Override
	Stream<? extends LinkNodeInternal<?, I>> streamChildren();
	@Override
	LinkNodeInternal<?, I> root();

	@Override
	Relation<T, ?> containingRelation();
	@Override
	Group<T> group();
	@Override
	I pNode();
	@Override
	T build();
}
