package org.logoce.lmf.cli.diagnostics;

import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;

import java.util.Objects;

/**
 * A diagnostic annotated with its source (typically a model path, but can also be {@code <subtree>}).
 */
public record DiagnosticItem(String file, LmDiagnostic diagnostic)
{
	public DiagnosticItem
	{
		Objects.requireNonNull(file, "file");
		Objects.requireNonNull(diagnostic, "diagnostic");
	}
}

