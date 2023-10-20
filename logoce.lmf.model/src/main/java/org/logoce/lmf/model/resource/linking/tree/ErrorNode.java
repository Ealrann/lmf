package org.logoce.lmf.model.resource.linking.tree;

import org.logoce.lmf.model.resource.linking.exception.LinkException;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.tree.AbstractTree;

public final class ErrorNode<I extends PNode> extends AbstractTree<LinkNode<I>> implements LinkNode<I>
{
	private final I pNode;
	private final LinkException exception;

	public ErrorNode(final I pNode, final LinkNode<I> parent, final LinkException exception)
	{
		super(parent);
		this.pNode = pNode;
		this.exception = exception;
	}

	@Override
	public I pNode()
	{
		return pNode;
	}

	public LinkException getException()
	{
		return exception;
	}
}
