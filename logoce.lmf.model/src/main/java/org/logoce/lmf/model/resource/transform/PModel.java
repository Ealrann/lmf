package org.logoce.lmf.model.resource.transform;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.resource.parsing.PNode;

import java.util.List;

public record PModel<I extends PNode>(List<? extends LinkNode<?, I>> trees)
{
	public List<? extends LMObject> build()
	{
		return trees.stream().map(LinkNode::build).toList();
	}
}
