package org.logoce.lmf.model.loader.linking.tree;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.loader.linking.LinkNode;
import org.logoce.lmf.model.loader.linking.ResolutionAttempt;
import org.logoce.lmf.model.loader.linking.linker.NodeLinker;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.tree.StructuredTree;

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
	Relation<T, ?> containingRelation();

	@Override
	Group<T> group();

	@Override
	I pNode();

	@Override
	T build();
}

