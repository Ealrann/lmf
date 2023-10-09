package org.logoce.lmf.model.resource.transform;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.resource.linking.LinkerNode;

import java.util.List;

public record PModel(List<LinkerNode<LMObject>> trees)
{
	public List<? extends LMObject> build()
	{
		return trees.stream().map(LinkerNode::build).toList();
	}
}
