package org.logoce.lmf.core.api.service;

public final class LmLoadException extends Exception
{
	public LmLoadException(final String message)
	{
		super(message);
	}

	public LmLoadException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}

