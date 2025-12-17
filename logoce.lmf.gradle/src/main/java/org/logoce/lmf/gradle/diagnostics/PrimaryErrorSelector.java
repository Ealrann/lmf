package org.logoce.lmf.gradle.diagnostics;

import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Picks the most helpful error diagnostic to surface as the primary failure.
 */
final class PrimaryErrorSelector
{
	record Entry(File file, LmDiagnostic diagnostic)
	{}

	private PrimaryErrorSelector()
	{
	}

	static Optional<Entry> selectPrimary(final List<ModelInspectionResult> inspections)
	{
		final List<Entry> errors = new ArrayList<>();

		for (final var inspection : inspections)
		{
			for (final var diagnostic : inspection.diagnostics())
			{
				if (diagnostic.severity() == LmDiagnostic.Severity.ERROR)
				{
					errors.add(new Entry(inspection.file(), diagnostic));
				}
			}
		}

		if (errors.isEmpty()) return Optional.empty();

		return errors.stream().min(Comparator.comparingInt(PrimaryErrorSelector::score));
	}

	private static int score(final Entry entry)
	{
		final var diagnostic = entry.diagnostic();
		int penalty = 0;

		if (diagnostic.line() == 1 && diagnostic.column() == 1)
		{
			penalty += 10;
		}

		final var message = diagnostic.message() == null ? "" : diagnostic.message();
		if (message.contains("Cannot resolve model '"))
		{
			penalty += 5;
		}
		if (message.contains("Cannot resolve imported model '"))
		{
			penalty += 5;
		}
		if ("Link error".equals(message))
		{
			penalty += 5;
		}

		return penalty;
	}
}
