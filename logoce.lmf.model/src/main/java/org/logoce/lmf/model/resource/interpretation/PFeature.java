package org.logoce.lmf.model.resource.interpretation;

import java.util.List;
import java.util.Optional;

public record PFeature(Optional<String> name, List<String> values, boolean isRelation) implements PNominalGroup
{
	public static PFeature of(Optional<String> name, List<String> values)
	{
		final var firstVal = values.get(0);
		final var firstChar = firstVal.charAt(0);
		final var isRelation = firstChar == '@' || firstChar == '#' || firstChar == '.' || firstChar == '/';
		return new PFeature(name, List.copyOf(values), isRelation);
	}

	@Override
	public String firstToken()
	{
		return name().orElse(values().get(0));
	}
}
