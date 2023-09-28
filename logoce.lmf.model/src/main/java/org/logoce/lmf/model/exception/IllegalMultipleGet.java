package org.logoce.lmf.model.exception;

import org.logoce.lmf.model.lang.Feature;

public class IllegalMultipleGet extends RuntimeException
{
	public IllegalMultipleGet(Feature<?, ?> feature)
	{
		super("Feature " + feature.name() + " is a relation multiple, use getList().");
	}

	public static final Object throwException(Feature<?, ?> feature)
	{
		throw new IllegalMultipleGet(feature);
	}
}
