package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FmtInPlaceTest
{
	@Test
	void fmtInPlaceRewritesFile(@TempDir final Path workspace) throws Exception
	{
		final var file = workspace.resolve("Model.lm");
		final var source = "(Root domain=test.model name=Bad metamodels=unknown.Meta (Child foo=bar))\n";
		Files.writeString(file, source);

		final var outBuffer = new ByteArrayOutputStream();
		final var errBuffer = new ByteArrayOutputStream();
		final var cli = new LmCli(new PrintStream(outBuffer), new PrintStream(errBuffer), workspace);

		final var before = Files.readString(file);
		final var exit = cli.run(new String[] { "fmt", "Model.lm", "--syntax-only", "--in-place" });
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var after = Files.readString(file);
		assertNotEquals(before, after);
		assertTrue(after.contains("\n\t("), after);

		final var stdout = outBuffer.toString(StandardCharsets.UTF_8);
		assertTrue(stdout.contains("OK: formatted Model.lm"), stdout);
	}
}

