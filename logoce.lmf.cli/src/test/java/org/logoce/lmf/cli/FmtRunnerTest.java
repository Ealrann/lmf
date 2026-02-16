package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.format.FmtRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FmtRunnerTest
{
	@Test
	void fmtSyntaxOnlyFormatsWithoutLinking(@TempDir final Path workspace) throws Exception
	{
		final var source = """
			(Root domain=test.model name=Bad metamodels=unknown.Meta
				(Child foo=bar))
			""";
		Files.writeString(workspace.resolve("InvalidSemantic.lm"), source);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new FmtRunner().run(context,
											"InvalidSemantic.lm",
											new FmtRunner.Options(null, false, true, false, false));
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);
		assertTrue(outBuffer.toString().contains("(Root domain=test.model name=Bad metamodels=unknown.Meta"),
				   "output:\n" + outBuffer);
	}

	@Test
	void fmtSyntaxOnlyRejectsRootAndRefPathToName(@TempDir final Path workspace) throws Exception
	{
		final var source = "(A)\n";
		Files.writeString(workspace.resolve("Simple.lm"), source);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new FmtRunner().run(context,
											"Simple.lm",
											new FmtRunner.Options("/x", true, true, false, false));
		assertEquals(ExitCodes.USAGE, exit);
		final var err = errBuffer.toString();
		assertTrue(err.contains("--syntax-only"), "err:\n" + err);
	}
}
