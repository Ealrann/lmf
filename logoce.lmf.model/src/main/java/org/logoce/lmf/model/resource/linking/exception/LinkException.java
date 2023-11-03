package org.logoce.lmf.model.resource.linking.exception;

import org.logoce.lmf.model.resource.parsing.PNode;

public class LinkException extends RuntimeException
{
	public final PNode pNode;

	public LinkException(final String message, final PNode pNode)
	{
		super(message);
		this.pNode = pNode;
	}
}
