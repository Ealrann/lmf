package org.logoce.lmf.cli.format;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class LmSourceFormatterTest
{
	@Test
	void formatOrOriginalRejectsNullSource()
	{
		final var formatter = new LmSourceFormatter();
		assertThrows(NullPointerException.class, () -> formatter.formatOrOriginal(null));
	}

	@Test
	void formatAllRejectsNullSources()
	{
		final var formatter = new LmSourceFormatter();
		final var sources = new LinkedHashMap<java.nio.file.Path, String>();
		sources.put(java.nio.file.Path.of("model.lm"), null);

		assertThrows(NullPointerException.class, () -> formatter.formatAll(sources));
	}
}
