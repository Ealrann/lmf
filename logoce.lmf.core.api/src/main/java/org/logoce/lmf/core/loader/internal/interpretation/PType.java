package org.logoce.lmf.core.loader.internal.interpretation;

import java.util.Optional;

public record PType(Optional<String> name, Optional<String> value) implements PNominalGroup
{
	@Override
	public String firstToken()
	{
		return name().orElse(value().orElseThrow());
	}
}
