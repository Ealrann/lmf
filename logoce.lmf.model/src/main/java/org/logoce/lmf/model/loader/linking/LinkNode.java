package org.logoce.lmf.model.loader.linking;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.parsing.PNode;

import java.util.List;

public interface LinkNode<T extends LMObject, I extends PNode>
{
	I pNode();

	Relation<T, ?, ?, ?> containingRelation();

	Group<T> group();

	List<ResolutionAttempt<Attribute<?, ?, ?, ?>>> attributeResolutions();

	List<ResolutionAttempt<Relation<?, ?, ?, ?>>> relationResolutions();

	LinkNode<?, I> parent();

	LinkNode<?, I> root();

	T build();
}
