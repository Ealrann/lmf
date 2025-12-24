package org.logoce.lmf.core.loader.interpretation;

import java.util.List;
import java.util.Optional;

public record PFeature(Optional<String> name, List<String> values, boolean isRelation) implements PNominalGroup
{
	public static PFeature of(Optional<String> name, List<String> values)
	{
		return of(name, values, false);
	}

	public static PFeature of(Optional<String> name, List<String> values, boolean forceAttribute)
	{
		if (values.isEmpty())
		{
			throw new IllegalArgumentException("Empty value list for feature '" + name.orElse("<anonymous>") + "'");
		}

		final var firstVal = values.getFirst();
		final boolean isRelation;
		if (forceAttribute)
		{
			isRelation = false;
		}
		else if (firstVal.isEmpty())
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
