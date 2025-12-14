package org.logoce.lmf.core.resource.interpretation;

import java.util.List;
import java.util.Optional;

public record PFeature(Optional<String> name, List<String> values, boolean isRelation) implements PNominalGroup
{
	public static PFeature of(Optional<String> name, List<String> values)
	{
		if (values.isEmpty())
		{
			throw new IllegalArgumentException("Empty value list for feature '" + name.orElse("<anonymous>") + "'");
		}

		final var firstVal = values.getFirst();
		final boolean isRelation;
		if (firstVal.isEmpty())
		{
			isRelation = false;
		}
		else
		{
			final var firstChar = firstVal.charAt(0);
			isRelation = firstChar == '@'
						 || firstChar == '#'
						 || firstChar == '.'
						 || firstChar == '/'
						 || firstChar == '^';
		}
		return new PFeature(name, List.copyOf(values), isRelation);
	}

	@Override
	public String firstToken()
	{
		return name().orElse(values().get(0));
	}

	public boolean isAttribute()
	{
		return !isRelation;
	}
}
