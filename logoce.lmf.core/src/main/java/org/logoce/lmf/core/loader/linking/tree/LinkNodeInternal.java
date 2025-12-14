package org.logoce.lmf.core.loader.linking.tree;

import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.linking.LinkNode;
import org.logoce.lmf.core.loader.linking.linker.NodeLinker;
import org.logoce.lmf.core.resource.interpretation.PFeature;
import org.logoce.lmf.core.resource.parsing.PNode;
import org.logoce.lmf.core.util.tree.StructuredTree;

import java.util.List;

public interface LinkNodeInternal<T extends LMObject, I extends PNode, S extends LinkNodeInternal<?, I, S>>
	extends LinkNode<T, I>, StructuredTree<S>
{
	List<PFeature> features();

	void resolveReferences(NodeLinker nodeLinker);

	@Override
	S parent();

	@Override
	S root();

	@Override
	Relation<T, ?, ?, ?> containingRelation();

	@Override
	Group<T> group();

	@Override
	I pNode();

	@Override
	T build();
}
