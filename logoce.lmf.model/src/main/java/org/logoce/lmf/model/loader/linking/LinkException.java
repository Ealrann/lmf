package org.logoce.lmf.model.loader.linking;

import org.logoce.lmf.model.resource.parsing.PNode;

public final class LinkException extends IllegalStateException
{
	private static final long serialVersionUID = 1L;

	private final transient PNode pNode;

	public LinkException(final String message, final PNode pNode)
	{
		super(message);
		this.pNode = pNode;
	}

	public PNode pNode()
	{
		return pNode;
	}
}
