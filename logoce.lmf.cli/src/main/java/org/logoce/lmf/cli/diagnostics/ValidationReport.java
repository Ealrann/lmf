package org.logoce.lmf.cli.diagnostics;

import java.util.List;

public record ValidationReport(boolean ok, List<DiagnosticItem> diagnostics, List<String> messages)
{
	public ValidationReport
	{
		diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
		messages = messages == null ? List.of() : List.copyOf(messages);
	}

	public static ValidationReport success()
	{
		return new ValidationReport(true, List.of(), List.of());
	}
}
