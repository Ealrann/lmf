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

final class CheckCommandJsonTest
{
	@Test
	void checkSingleJsonOutputsValidJson(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("Good.lm"), """
			(MetaModel domain=test.model name=Good
				(Unit name=u))
			""");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var command = CheckCommand.parse(List.of("Good.lm", "--json"), context.err());
		final int exit = command.execute(context);
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString().trim();
		assertTrue(stdout.contains("\"command\":\"check\""), stdout);
		assertTrue(stdout.contains("\"mode\":\"single\""), stdout);
		assertTrue(stdout.contains("Good.lm"), stdout);
		JsonTestUtil.assertValidJson(stdout);
	}

	@Test
	void checkAllJsonOutputsValidJson(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("Good.lm"), """
			(MetaModel domain=test.model name=Good
				(Unit name=u))
			""");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var command = CheckCommand.parse(List.of("--all", "--json"), context.err());
		final int exit = command.execute(context);
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString().trim();
		assertTrue(stdout.contains("\"command\":\"check\""), stdout);
		assertTrue(stdout.contains("\"mode\":\"all\""), stdout);
		assertTrue(stdout.contains("\"results\""), stdout);
		JsonTestUtil.assertValidJson(stdout);
	}

	@Test
	void checkJsonReportsImportCycleWithoutCrashing(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("Application.lm"), """
			(MetaModel domain=test.cycle name=Application imports=test.cycle.UI
				(Unit name=u))
			""");
		Files.writeString(workspace.resolve("UI.lm"), """
			(MetaModel domain=test.cycle name=UI imports=test.cycle.Application
				(Unit name=u))
			""");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var command = CheckCommand.parse(List.of("Application.lm", "--json"), context.err());
		final int exit = command.execute(context);
		assertEquals(ExitCodes.INVALID, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString().trim();
		JsonTestUtil.assertValidJson(stdout);
		assertTrue(stdout.toLowerCase().contains("import cycle"), stdout);
		assertFalse(errBuffer.toString().contains("IllegalStateException"), "stderr should not contain a stack trace:\n" + errBuffer);
	}

	@Test
	void checkJsonReportsDuplicateMetaModelQualifiedName(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("MM1.lm"), """
			(MetaModel domain=test.dup name=MM
				(Definition Root (includes group=#LMCore@Model)
					(+att name=foo datatype=#LMCore@string [0..1])))
			""");
		Files.writeString(workspace.resolve("MM2.lm"), """
			(MetaModel domain=test.dup name=MM
				(Definition Root (includes group=#LMCore@Model)
					(+att name=foo datatype=#LMCore@string [0..1])))
			""");
		Files.writeString(workspace.resolve("Use.lm"), """
			(Root domain=test.dup name=Use metamodels=test.dup.MM foo=bar)
			""");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var command = CheckCommand.parse(List.of("Use.lm", "--json"), context.err());
		final int exit = command.execute(context);
		assertEquals(ExitCodes.INVALID, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString().trim();
		JsonTestUtil.assertValidJson(stdout);
		assertTrue(stdout.contains("Duplicate meta-model qualified name"), stdout);
	}
}
