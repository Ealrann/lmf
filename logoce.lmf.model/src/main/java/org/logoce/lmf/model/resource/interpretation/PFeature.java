package org.logoce.lmf.model.resource.interpretation;

import java.util.List;
import java.util.Optional;

public record PFeature(Optional<String> name, List<String> values) implements PNominalGroup
{
	@Override
	public String firstToken()
	{
		return name().orElse(values().get(0));
	}
}
