package org.logoce.lmf.cli.diagnostics;

import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;

import java.io.PrintWriter;
import java.util.List;

public final class DiagnosticReporter
{
	private DiagnosticReporter()
	{
	}

	public static boolean hasErrors(final List<LmDiagnostic> diagnostics)
	{
		return diagnostics.stream()
						  .anyMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR);
	}

	public static void printDiagnostics(final PrintWriter err,
										final String source,
										final List<LmDiagnostic> diagnostics)
	{
		if (diagnostics.isEmpty())
		{
			return;
		}
		err.println("Diagnostics in " + source + ":");
		for (final var diag : diagnostics)
		{
			printDiagnostic(err, source, diag);
		}
	}

	private static void printDiagnostic(final PrintWriter err,
										final String source,
										final LmDiagnostic diag)
	{
		err.printf("%s:%d:%d [%s] %s%n",
				   source,
				   diag.line(),
				   diag.column(),
				   diag.severity(),
				   diag.message());
	}
}
