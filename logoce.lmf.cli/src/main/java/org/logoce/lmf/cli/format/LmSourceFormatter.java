package org.logoce.lmf.cli.format;

import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class LmSourceFormatter
{
	private final LmFormatter formatter = new LmFormatter();
	private final LmTreeReader reader = new LmTreeReader();

	public String formatOrOriginal(final String source)
	{
		Objects.requireNonNull(source, "source");

		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var readResult = reader.read(source, diagnostics);
		if (DiagnosticReporter.hasErrors(diagnostics) || readResult.roots().isEmpty())
		{
			return source;
		}

		final var formatted = formatter.format(readResult.roots());
		return formatted.endsWith("\n") ? formatted : formatted + "\n";
	}

	public Map<Path, String> formatAll(final Map<Path, String> sourcesByPath)
	{
		Objects.requireNonNull(sourcesByPath, "sourcesByPath");

		final var formatted = new LinkedHashMap<Path, String>(sourcesByPath.size());
		for (final var entry : sourcesByPath.entrySet())
		{
			formatted.put(entry.getKey(), formatOrOriginal(entry.getValue()));
		}
		return Map.copyOf(formatted);
	}
}
