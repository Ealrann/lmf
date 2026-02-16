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

final class CheckAllCommandTest
{
	@Test
	void checkAllReturnsInvalidWhenAnyModelIsInvalid(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("Good.lm"), """
			(MetaModel domain=test.model name=Good
				(Unit name=u))
			""");
		Files.writeString(workspace.resolve("Bad.lm"), "(MetaModel domain=test.model name=Bad");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var command = CheckCommand.parse(List.of("--all"), context.err());
		final int exit = command.execute(context);

		assertEquals(ExitCodes.INVALID, exit);
	}

	@Test
	void checkAllExcludeSkipsFiles(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("Good.lm"), """
			(MetaModel domain=test.model name=Good
				(Unit name=u))
			""");
		Files.createDirectories(workspace.resolve("snippets"));
		Files.writeString(workspace.resolve("snippets/Bad.lm"), "(MetaModel domain=test.model name=Bad");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var command = CheckCommand.parse(List.of("--all", "--exclude", "snippets"), context.err());
		final int exit = command.execute(context);

		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);
	}
}
