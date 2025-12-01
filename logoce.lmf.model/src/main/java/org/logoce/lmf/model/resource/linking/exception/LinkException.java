package org.logoce.lmf.model.resource.linking.exception;

import org.logoce.lmf.model.resource.parsing.PNode;

public class LinkException extends IllegalStateException
{
	private static final long serialVersionUID = 1L;

	public final transient PNode pNode;

	public LinkException(final String message, final PNode pNode)
	{
		super(message);
		this.pNode = pNode;
	}
}
