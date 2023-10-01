package org.logoce.lmf.model.resource.transform.parsing;

import java.util.List;
import java.util.Optional;

public interface ParsedToken
{
	Optional<String> name();
	List<String> values();

	default String firstToken()
	{
		return name().orElse(values().get(0));
	}
}
