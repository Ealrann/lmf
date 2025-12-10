package org.logoce.lmf.model.util;


import org.logoce.lmf.model.api.model.IFeaturedObject;

import java.util.List;
import java.util.stream.Stream;

public record ReferenceExplorer(int reference)
{
	@SuppressWarnings("unchecked")
	public Stream<IFeaturedObject> stream(IFeaturedObject object)
	{
		final var val = getValue(object);
		if (val instanceof List)
		{
			return ((List<IFeaturedObject>) val).stream();
		}
		else
		{
			return Stream.ofNullable((IFeaturedObject) val);
		}
	}

	private Object getValue(IFeaturedObject target)
	{
		return target.get(reference);
	}
}
