package org.logoce.lmf.cli.edit;

import org.logoce.lmf.cli.diagnostics.ValidationReport;

import java.nio.file.Path;
import java.util.Map;

public record EditOutcome(Map<Path, String> sources,
						  boolean validationPassed,
						  boolean wrote,
						  boolean forcedWrite,
						  ValidationReport validationReport)
{
	public static EditOutcome noChanges()
	{
		return new EditOutcome(Map.of(), true, false, false, ValidationReport.success());
	}

	public boolean changed()
	{
		return sources != null && !sources.isEmpty();
	}
}
