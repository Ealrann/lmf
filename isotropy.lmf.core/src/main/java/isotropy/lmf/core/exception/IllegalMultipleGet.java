package isotropy.lmf.core.exception;

import isotropy.lmf.core.lang.Feature;

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
