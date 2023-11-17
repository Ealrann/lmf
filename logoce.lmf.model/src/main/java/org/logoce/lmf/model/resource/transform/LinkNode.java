package org.logoce.lmf.model.resource.transform;

import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.linking.tree.LinkNodeInternal;
import org.logoce.lmf.model.resource.parsing.PNode;

import java.util.List;
import java.util.stream.Stream;

public interface LinkNode<T extends LMObject, I extends PNode>
{
	I pNode();
	Relation<T, ?> containingRelation();
	Group<T> group();
	List<ResolutionAttempt> tokenResolutions();
	LinkNodeInternal<?, I> parent();
	Stream<? extends LinkNodeInternal<?, I>> streamChildren();
	LinkNodeInternal<?, I> root();

	T build();
}
