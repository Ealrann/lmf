package org.logoce.lmf.core.util.oldlogoce;

import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;

import java.util.List;
import java.util.stream.Stream;

public record ReferenceExplorer(Relation<?, ?, ?, ?> reference)
{
	@SuppressWarnings("unchecked")
	public Stream<LMObject> stream(LMObject object)
	{
		final var val = getValue(object);
		if (val instanceof List)
		{
			return ((List<LMObject>) val).stream();
		}
		else
		{
			return Stream.ofNullable((LMObject) val);
		}
	}

	private Object getValue(LMObject target)
	{
		return target.get(reference);
	}
}
