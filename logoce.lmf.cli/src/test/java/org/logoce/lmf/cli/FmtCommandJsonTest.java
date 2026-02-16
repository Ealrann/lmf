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

final class FmtCommandJsonTest
{
	@Test
	void fmtJsonOutputsValidJson(@TempDir final Path workspace) throws Exception
	{
		final var source = "(Root domain=test.model name=Bad metamodels=unknown.Meta (Child foo=bar))\n";
		Files.writeString(workspace.resolve("Model.lm"), source);

		final var outBuffer = new ByteArrayOutputStream();
		final var errBuffer = new ByteArrayOutputStream();
		final var cli = new LmCli(new PrintStream(outBuffer), new PrintStream(errBuffer), workspace);

		final var exit = cli.run(new String[] { "fmt", "Model.lm", "--syntax-only", "--json" });
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString(StandardCharsets.UTF_8).trim();
		assertTrue(stdout.contains("\"command\":\"fmt\""), stdout);
		assertTrue(stdout.contains("\"formatted\""), stdout);
		JsonTestUtil.assertValidJson(stdout);
	}
}

