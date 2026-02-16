package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.replace.ReplaceRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ReplaceRunnerTest
{
	@Test
	void replaceWritesOnlyWhenModelIsValid(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);
		final var file = workspace.resolve("ModelA.lm");
		final var original = Files.readString(file);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new ReplaceRunner().run(context,
												 "ModelA.lm",
												 "/materials/materials.1",
												 "(Material name=Stone)",
												 new ReplaceRunner.Options(false, false));

		assertEquals(ExitCodes.OK, exit);
		final var updated = Files.readString(file);
		assertNotEquals(original, updated);
		assertTrue(updated.contains("Material name=Stone"));
	}

	@Test
	void replaceRejectsMultipleRootElements(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);
		final var file = workspace.resolve("ModelA.lm");
		final var original = Files.readString(file);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new ReplaceRunner().run(context,
												 "ModelA.lm",
												 "/materials/materials.1",
												 "(Material name=Stone) (Material name=Other)",
												 new ReplaceRunner.Options(false, false));

		assertEquals(ExitCodes.USAGE, exit);
		final var updated = Files.readString(file);
		assertEquals(original, updated);
		assertTrue(errBuffer.toString().contains("exactly one root element"));
	}

	@Test
	void replaceParseFailurePrintsHint(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);
		final var file = workspace.resolve("ModelA.lm");
		final var original = Files.readString(file);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new ReplaceRunner().run(context,
												 "ModelA.lm",
												 "/materials/materials.1",
												 "(Material name=Stone",
												 new ReplaceRunner.Options(false, false));

		assertEquals(ExitCodes.USAGE, exit, "err:\n" + errBuffer);
		assertTrue(errBuffer.toString().contains("Replacement subtree cannot be parsed"), "err:\n" + errBuffer);
		assertTrue(errBuffer.toString().contains("Hint: If you passed a subtree inline"), "err:\n" + errBuffer);

		final var updated = Files.readString(file);
		assertEquals(original, updated);
	}

	@Test
	void replaceDoesNotWriteWhenReplacementMakesModelInvalid(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);
		final var file = workspace.resolve("ModelA.lm");
		final var original = Files.readString(file);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new ReplaceRunner().run(context,
												 "ModelA.lm",
												 "/materials/materials.0",
												 "(Material name=Stone)",
												 new ReplaceRunner.Options(false, false));

		assertEquals(ExitCodes.INVALID, exit);
		final var updated = Files.readString(file);
		assertEquals(original, updated);
		assertTrue(errBuffer.toString().contains("No changes written"));
	}

	@Test
	void replaceForceWritesEvenWhenModelIsInvalid(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);
		final var file = workspace.resolve("ModelA.lm");
		final var original = Files.readString(file);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new ReplaceRunner().run(context,
												 "ModelA.lm",
												 "/materials/materials.0",
												 "(Material name=Stone)",
												 new ReplaceRunner.Options(true, false));

		assertEquals(ExitCodes.INVALID, exit);
		final var updated = Files.readString(file);
		assertNotEquals(original, updated);
		assertTrue(updated.contains("Material name=Stone"));
		assertTrue(errBuffer.toString().contains("Diagnostics in"), "err:\n" + errBuffer);
		assertTrue(errBuffer.toString().contains("FORCED: wrote changes"), "err:\n" + errBuffer);
	}

	private static void writeModels(final Path workspace) throws Exception
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
