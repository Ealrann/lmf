package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.command.CheckCommand;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CheckAllVerboseOutputTest
{
	@Test
	void checkAllIsErrorsOnlyByDefault(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("GoodA.lm"), """
			(MetaModel domain=test.model name=GoodA
				(Unit name=u))
			""");
		Files.writeString(workspace.resolve("GoodB.lm"), """
			(MetaModel domain=test.model name=GoodB
				(Unit name=u))
			""");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var command = CheckCommand.parse(List.of("--all"), context.err());
		final int exit = command.execute(context);

		assertEquals(ExitCodes.OK, exit);

		final var out = outBuffer.toString();
		assertFalse(out.contains("OK: GoodA.lm"), out);
		assertFalse(out.contains("OK: GoodB.lm"), out);
		assertTrue(out.contains("OK: checked"), out);
	}

	@Test
	void checkAllVerbosePrintsOkPerFile(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("GoodA.lm"), """
			(MetaModel domain=test.model name=GoodA
				(Unit name=u))
			""");
		Files.writeString(workspace.resolve("GoodB.lm"), """
			(MetaModel domain=test.model name=GoodB
				(Unit name=u))
			""");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var command = CheckCommand.parse(List.of("--all", "--verbose"), context.err());
		final int exit = command.execute(context);

		assertEquals(ExitCodes.OK, exit);

		final var out = outBuffer.toString();
		assertTrue(out.contains("OK: GoodA.lm"), out);
		assertTrue(out.contains("OK: GoodB.lm"), out);
		assertTrue(out.contains("OK: checked"), out);
	}
}

