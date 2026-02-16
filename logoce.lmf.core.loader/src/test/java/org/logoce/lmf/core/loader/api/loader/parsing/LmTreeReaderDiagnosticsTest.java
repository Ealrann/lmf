package org.logoce.lmf.core.loader.api.loader.parsing;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class LmTreeReaderDiagnosticsTest
{
	@Test
	void missingClosingParensReportsCountAndInnermostOpenLocation()
	{
		final var source = "(Root\n(Child";
		final var diagnostics = new ArrayList<LmDiagnostic>();

		new LmTreeReader().read(source, diagnostics);

		final var combined = diagnostics.stream()
										.map(LmDiagnostic::message)
										.reduce("", (a, b) -> a + "\n" + b);

		assertTrue(combined.contains("Unexpected end of input"), combined);
		assertTrue(combined.contains("missing 2 ')'"), combined);
		assertTrue(combined.contains("innermost '(' opened at 2:1"), combined);
	}

	@Test
	void missingClosingParenReportsInnermostOpenLocation()
	{
		final var source = "(Root\n(Child)";
		final var diagnostics = new ArrayList<LmDiagnostic>();

		new LmTreeReader().read(source, diagnostics);

		final var combined = diagnostics.stream()
										.map(LmDiagnostic::message)
										.reduce("", (a, b) -> a + "\n" + b);

		assertTrue(combined.contains("Unexpected end of input"), combined);
		assertTrue(combined.contains("missing ')'"), combined);
		assertTrue(combined.contains("innermost '(' opened at 1:1"), combined);
	}
}

