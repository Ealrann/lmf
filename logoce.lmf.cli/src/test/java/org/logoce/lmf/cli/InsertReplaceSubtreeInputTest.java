package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.command.InsertCommand;
import org.logoce.lmf.cli.command.ReplaceCommand;
import org.logoce.lmf.cli.insert.InsertRunner;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class InsertReplaceSubtreeInputTest
{
	@Test
	void insertReadsSubtreeFromFile(@TempDir final Path workspace) throws Exception
	{
		writeInsertModels(workspace);
		Files.writeString(workspace.resolve("subtree.lm"),
						  """
							(Material name="Sand \\"Q\\"")
							""");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var command = InsertCommand.parse(List.of("ModelA.lm",
													   "/materials/materials.1",
													   "--subtree-file",
													   "subtree.lm"),
											   context.err());
		assertNotNull(command);

		final int exit = command.execute(context);
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var updated = Files.readString(workspace.resolve("ModelA.lm"));
		assertTrue(updated.contains("Material name=\"Sand \\\"Q\\\"\""), "updated:\n" + updated);
	}

	@Test
	void insertReadsSubtreeFromStdin(@TempDir final Path workspace) throws Exception
	{
		writeInsertModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var previousIn = System.in;
		try
		{
			System.setIn(new ByteArrayInputStream("(Material name=Sand)".getBytes(StandardCharsets.UTF_8)));

			final var command = InsertCommand.parse(List.of("ModelA.lm",
														   "/materials/materials.1",
														   "--subtree-stdin"),
												   context.err());
			assertNotNull(command);

			final int exit = command.execute(context);
			assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);
		}
		finally
		{
			System.setIn(previousIn);
		}

		final var updated = Files.readString(workspace.resolve("ModelA.lm"));
		assertTrue(updated.contains("Material name=Sand"), "updated:\n" + updated);
	}

	@Test
	void replaceReadsSubtreeFromStdinDashArg(@TempDir final Path workspace) throws Exception
	{
		writeReplaceModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var previousIn = System.in;
		try
		{
			System.setIn(new ByteArrayInputStream("(Material name=\"Stone \\\"Q\\\"\")".getBytes(StandardCharsets.UTF_8)));

			final var command = ReplaceCommand.parse(List.of("ModelA.lm",
															"/materials/materials.1",
															"-"),
													context.err());
			assertNotNull(command);

			final int exit = command.execute(context);
			assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);
		}
		finally
		{
			System.setIn(previousIn);
		}

		final var updated = Files.readString(workspace.resolve("ModelA.lm"));
		assertTrue(updated.contains("Material name=\"Stone \\\"Q\\\"\""), "updated:\n" + updated);
	}

	@Test
	void insertInlineSubtreeEmitsDeprecationWarning(@TempDir final Path workspace) throws Exception
	{
		writeInsertModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var command = InsertCommand.parse(List.of("ModelA.lm",
													   "/materials/materials.1",
													   "(Material name=Sand)"),
											   context.err());
		assertNotNull(command);

		final int exit = command.execute(context);
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);
		assertTrue(errBuffer.toString().contains("inline <subtree> is deprecated"), "err:\n" + errBuffer);

		final var updated = Files.readString(workspace.resolve("ModelA.lm"));
		assertTrue(updated.contains("Material name=Sand"), "updated:\n" + updated);
	}

	@Test
	void replaceInlineSubtreeEmitsDeprecationWarning(@TempDir final Path workspace) throws Exception
	{
		writeReplaceModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var command = ReplaceCommand.parse(List.of("ModelA.lm",
														"/materials/materials.1",
														"(Material name=Stone)"),
												context.err());
		assertNotNull(command);

		final int exit = command.execute(context);
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);
		assertTrue(errBuffer.toString().contains("inline <subtree> is deprecated"), "err:\n" + errBuffer);

		final var updated = Files.readString(workspace.resolve("ModelA.lm"));
		assertTrue(updated.contains("Material name=Stone"), "updated:\n" + updated);
	}

	@Test
	void insertJsonParseFailureIncludesDiagnostics(@TempDir final Path workspace) throws Exception
	{
		writeInsertModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new InsertRunner().run(context,
												"ModelA.lm",
												"/materials/materials.1",
												"(Material name=Sand",
												new InsertRunner.Options(true));
		assertEquals(ExitCodes.USAGE, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString().trim();
		JsonTestUtil.assertValidJson(stdout);
		assertTrue(stdout.contains("\"command\":\"insert\""), stdout);
		assertTrue(stdout.contains("\"diagnostics\""), stdout);
		assertTrue(stdout.contains("\"file\":\"<subtree>\""), stdout);
		assertTrue(errBuffer.toString().contains("Hint: If you passed a subtree inline"), "err:\n" + errBuffer);
	}

	private static void writeInsertModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=InsertMeta
				(Group Named (includes group=#LMCore@Named))
				(Definition Material (includes group=@Named))
				(Definition MaterialsEnv (includes group=#LMCore@LMObject)
					(+contains materials [0..*] @Material))
				(Definition Root (includes group=#LMCore@Model)
					(+contains materials [1..1] @MaterialsEnv)))
			""";

		final var model = """
			(Root domain=test.model name=ModelA metamodels=test.model.InsertMeta
				(MaterialsEnv
					(Material name=Lava)
					(Material name=Dirt)))
			""";

		Files.writeString(workspace.resolve("InsertMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), model);
	}

	private static void writeReplaceModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=RefMeta
				(Group Named (includes group=#LMCore@Named))
				(Definition Material (includes group=@Named))
				(Definition MaterialsEnv (includes group=#LMCore@LMObject)
					(+contains materials [0..*] @Material))
				(Definition Root (includes group=#LMCore@Model)
					(+contains materials [1..1] @MaterialsEnv)
					(+refers selected [0..1] @Material)))
			""";

		final var modelA = """
			(Root domain=test.model name=ModelA metamodels=test.model.RefMeta selected=@Lava
				(MaterialsEnv
					(Material name=Lava)
					(Material name=Dirt)))
			""";

		Files.writeString(workspace.resolve("RefMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), modelA);
	}
}
