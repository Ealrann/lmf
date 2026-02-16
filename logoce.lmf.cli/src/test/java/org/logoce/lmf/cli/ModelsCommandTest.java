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

final class ModelsCommandTest
{
	@Test
	void modelsJsonOutputsValidJson(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("A.lm"), """
			(MetaModel domain=test.models name=A
				(Unit name=u))
			""");
		Files.writeString(workspace.resolve("B.lm"), """
			(MetaModel domain=test.models name=B
				(Unit name=u))
			""");

		final var outBuffer = new ByteArrayOutputStream();
		final var errBuffer = new ByteArrayOutputStream();
		final var cli = new LmCli(new PrintStream(outBuffer), new PrintStream(errBuffer), workspace);

		final var exit = cli.run(new String[] { "models", "--json" });
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString(StandardCharsets.UTF_8).trim();
		JsonTestUtil.assertValidJson(stdout);
		assertTrue(stdout.contains("\"command\":\"models\""), stdout);
		assertTrue(stdout.contains("\"modelCount\":2"), stdout);
		assertTrue(stdout.contains("A.lm"), stdout);
		assertTrue(stdout.contains("test.models.A"), stdout);
	}

	@Test
	void modelsDuplicatesJsonReportsDuplicates(@TempDir final Path workspace) throws Exception
	{
		Files.createDirectories(workspace.resolve("a"));
		Files.createDirectories(workspace.resolve("b"));
		Files.createDirectories(workspace.resolve("c"));
		Files.createDirectories(workspace.resolve("d"));

		Files.writeString(workspace.resolve("a").resolve("Same.lm"), """
			(MetaModel domain=test.dupf name=One
				(Unit name=u))
			""");
		Files.writeString(workspace.resolve("b").resolve("Same.lm"), """
			(MetaModel domain=test.dupf name=Two
				(Unit name=u))
			""");

		Files.writeString(workspace.resolve("c").resolve("MM.lm"), """
			(MetaModel domain=test.qn name=MM
				(Unit name=u))
			""");
		Files.writeString(workspace.resolve("d").resolve("Other.lm"), """
			(MetaModel domain=test.qn name=MM
				(Unit name=u))
			""");

		final var outBuffer = new ByteArrayOutputStream();
		final var errBuffer = new ByteArrayOutputStream();
		final var cli = new LmCli(new PrintStream(outBuffer), new PrintStream(errBuffer), workspace);

		final var exit = cli.run(new String[] { "models", "--duplicates", "--json" });
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString(StandardCharsets.UTF_8).trim();
		JsonTestUtil.assertValidJson(stdout);
		assertTrue(stdout.contains("\"duplicateFileNames\""), stdout);
		assertTrue(stdout.contains("\"duplicateQualifiedNames\""), stdout);
		assertTrue(stdout.contains("\"fileName\":\"Same.lm\""), stdout);
		assertTrue(stdout.contains("\"qualifiedName\":\"test.qn.MM\""), stdout);
	}
}

