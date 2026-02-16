package org.logoce.lmf.core.loader.api.loader.linking;

import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;

public final class LinkException extends IllegalStateException
{
	private static final long serialVersionUID = 1L;

	private final transient PNode pNode;
	private final transient PToken token;

	public LinkException(final String message, final PNode pNode)
	{
		this(message, pNode, null);
	}

	public LinkException(final String message, final PNode pNode, final PToken token)
	{
		super(message);
		this.pNode = pNode;
		this.token = token;
	}

	public PNode pNode()
	{
		return pNode;
	}

	public PToken token()
	{
		return token;
	}
}
