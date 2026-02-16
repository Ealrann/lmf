package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class QualifiedModelSpecTest
{
	@Test
	void checkAcceptsQualifiedNameModelSpec(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("Good.lm"), """
			(MetaModel domain=test.model name=Good
				(Unit name=u))
			""");

		final var outBuffer = new ByteArrayOutputStream();
		final var errBuffer = new ByteArrayOutputStream();
		final var cli = new LmCli(new PrintStream(outBuffer), new PrintStream(errBuffer), workspace);

		final var exit = cli.run(new String[] { "check", "qn:test.model.Good", "--json" });
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString(StandardCharsets.UTF_8).trim();
		JsonTestUtil.assertValidJson(stdout);
		assertTrue(stdout.contains("\"requested\":\"qn:test.model.Good\""), stdout);
		assertTrue(stdout.contains("\"path\":\"Good.lm\""), stdout);
	}

	@Test
	void checkAcceptsModelOption(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("Good.lm"), """
			(MetaModel domain=test.model name=Good
				(Unit name=u))
			""");

		final var outBuffer = new ByteArrayOutputStream();
		final var errBuffer = new ByteArrayOutputStream();
		final var cli = new LmCli(new PrintStream(outBuffer), new PrintStream(errBuffer), workspace);

		final var exit = cli.run(new String[] { "check", "--json", "--model", "test.model.Good" });
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString(StandardCharsets.UTF_8).trim();
		JsonTestUtil.assertValidJson(stdout);
		assertTrue(stdout.contains("\"requested\":\"qn:test.model.Good\""), stdout);
		assertTrue(stdout.contains("\"path\":\"Good.lm\""), stdout);
	}
}

