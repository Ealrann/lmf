package org.logoce.lmf.model.resource.linking.tree;

import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.linking.feature.NodeLinker;
import org.logoce.lmf.model.resource.parsing.PNode;

import java.util.List;
import java.util.stream.Stream;

public interface LinkNode<T extends LMObject, I extends PNode>
{
	List<PFeature> features();
	Relation<T, ?> containingRelation();
	LinkNode<?, I> parent();
	void linkTokens(final NodeLinker nodeLinker);
	Stream<? extends LinkNode<?, I>> streamChildren();
	LinkNode<?, I> root();
	Group<T> group();
	I pNode();
	T build();
}
