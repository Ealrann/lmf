package org.logoce.lmf.cli.edit;

import java.nio.file.Path;
import java.util.Map;

public record EditOutcome(Map<Path, String> sources,
						  boolean validationPassed,
						  boolean wrote,
						  boolean forcedWrite)
{
	public static EditOutcome noChanges()
	{
		return new EditOutcome(Map.of(), true, false, false);
	}

	public boolean changed()
	{
		return sources != null && !sources.isEmpty();
	}
}

