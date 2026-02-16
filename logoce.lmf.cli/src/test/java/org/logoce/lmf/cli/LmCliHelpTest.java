package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LmCliHelpTest
{
	@Test
	void globalHelpMentionsCheckAllExclude(@TempDir final Path workspace)
	{
		final var outBuffer = new ByteArrayOutputStream();
		final var errBuffer = new ByteArrayOutputStream();
		final var cli = new LmCli(new PrintStream(outBuffer), new PrintStream(errBuffer), workspace);

		final var exit = cli.run(new String[] { "--help" });
		assertEquals(ExitCodes.OK, exit);

		final var out = new String(outBuffer.toByteArray(), StandardCharsets.UTF_8);
		assertTrue(out.contains("check --all"), out);
		assertTrue(out.contains("--exclude"), out);
		assertTrue(out.contains("models"), out);
		assertTrue(out.contains("--model"), out);
	}

	@Test
	void batchHelpDoesNotReportUnknownCommand(@TempDir final Path workspace)
	{
		final var outBuffer = new ByteArrayOutputStream();
		final var errBuffer = new ByteArrayOutputStream();
		final var cli = new LmCli(new PrintStream(outBuffer), new PrintStream(errBuffer), workspace);

		final var exit = cli.run(new String[] { "batch", "--help" });
		assertEquals(ExitCodes.OK, exit);

		final var err = new String(errBuffer.toByteArray(), StandardCharsets.UTF_8);
		assertTrue(err.contains("Usage: lm"), err);
		assertFalse(err.contains("Unknown or invalid command"), err);
	}
}
