package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.insert.InsertRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class InsertRunnerTest
{
	@Test
	void insertIntoContainmentListShiftsReferencesInImporters(@TempDir final Path workspace) throws Exception
	{
		writeListModels(workspace);
		final var fileA = workspace.resolve("ModelA.lm");
		final var fileB = workspace.resolve("ModelB.lm");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new InsertRunner().run(context,
												"ModelA.lm",
												"/materials/materials.1",
												"(Material name=Sand)");

		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var updatedA = Files.readString(fileA);
		final var updatedB = Files.readString(fileB);

		assertTrue(updatedA.contains("Material name=Sand"), "updatedA:\n" + updatedA);
		assertTrue(updatedA.contains("Material name=Lava"), "updatedA:\n" + updatedA);
		assertTrue(updatedA.contains("Material name=Dirt"), "updatedA:\n" + updatedA);

		assertFalse(updatedA.contains("mainMaterial=/materials/materials.1"), "updatedA:\n" + updatedA);
		assertTrue(updatedA.contains("mainMaterial=/materials/materials.2"), "updatedA:\n" + updatedA);

		assertFalse(updatedA.contains("altMaterial=/materials/materials.2"), "updatedA:\n" + updatedA);
		assertTrue(updatedA.contains("altMaterial=/materials/materials.3"), "updatedA:\n" + updatedA);

		assertFalse(updatedB.contains("altMaterial=#ModelA/materials/materials.2"), "updatedB:\n" + updatedB);
		assertTrue(updatedB.contains("altMaterial=#ModelA/materials/materials.3"), "updatedB:\n" + updatedB);
	}

	@Test
	void insertIntoSingleContainmentFailsWhenAlreadyPresent(@TempDir final Path workspace) throws Exception
	{
		writeSingleModels(workspace);
		final var fileA = workspace.resolve("ModelA.lm");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var insert = new InsertRunner().run(context,
												  "ModelA.lm",
												  "/settings",
												  "(Settings)");
		assertEquals(ExitCodes.OK, insert, "err:\n" + errBuffer);

		final var afterInsert = Files.readString(fileA);
		assertTrue(afterInsert.contains("(Settings"), "updated:\n" + afterInsert);

		final var secondOut = new StringWriter();
		final var secondErr = new StringWriter();
		final var secondContext = new CliContext(workspace, new PrintWriter(secondOut), new PrintWriter(secondErr));

		final var second = new InsertRunner().run(secondContext,
												  "ModelA.lm",
												  "/settings",
												  "(Settings)");
		assertEquals(ExitCodes.INVALID, second, "err:\n" + secondErr);

		final var afterSecond = Files.readString(fileA);
		assertEquals(afterInsert, afterSecond, "file changed despite failure:\n" + afterSecond);
		assertTrue(secondErr.toString().contains("use replace"), "err:\n" + secondErr);
	}

	private static void writeListModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=InsertMeta
				(Group Named (includes group=#LMCore@Named))
				(Definition Material (includes group=@Named))
				(Definition MaterialsEnv (includes group=#LMCore@LMObject)
					(+contains materials [0..*] @Material))
				(Definition Root (includes group=#LMCore@Model)
					(+contains materials [1..1] @MaterialsEnv)
					(+refers mainMaterial [0..1] @Material)
					(+refers altMaterial [0..1] @Material)))
			""";

		final var modelA = """
			(Root domain=test.model name=ModelA metamodels=test.model.InsertMeta
				mainMaterial=/materials/materials.1
				altMaterial=/materials/materials.2
				(MaterialsEnv
					(Material name=Lava)
					(Material name=Dirt)
					(Material name=Stone)))
			""";

		final var modelB = """
			(Root domain=test.model name=ModelB metamodels=test.model.InsertMeta imports=test.model.ModelA
				altMaterial=#ModelA/materials/materials.2
				(MaterialsEnv))
			""";

		Files.writeString(workspace.resolve("InsertMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), modelA);
		Files.writeString(workspace.resolve("ModelB.lm"), modelB);
	}

	private static void writeSingleModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=SingleInsertMeta
				(Definition Settings (includes group=#LMCore@LMObject))
				(Definition Root (includes group=#LMCore@Model)
					(+contains settings [0..1] @Settings)))
			""";

		final var modelA = """
			(Root domain=test.model name=ModelA metamodels=test.model.SingleInsertMeta)
			""";

		Files.writeString(workspace.resolve("SingleInsertMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), modelA);
	}
}

