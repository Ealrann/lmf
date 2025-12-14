package org.logoce.lmf.core.loader.linking;

import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.resource.parsing.PNode;

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
