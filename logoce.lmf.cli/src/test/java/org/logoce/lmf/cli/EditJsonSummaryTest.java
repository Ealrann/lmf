package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.insert.InsertRunner;
import org.logoce.lmf.cli.remove.RemoveRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EditJsonSummaryTest
{
	@Test
	void removeJsonIncludesUnsetsAndReferenceRewrites(@TempDir final Path workspace) throws Exception
	{
		writeRemoveModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new RemoveRunner().run(context,
												"ModelA.lm",
												"/materials/materials.1",
												new RemoveRunner.Options(true));
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString().trim();
		assertTrue(stdout.contains("\"command\":\"remove\""), stdout);
		assertTrue(stdout.contains("\"unsets\""), stdout);
		assertTrue(stdout.contains("\"referenceRewrites\""), stdout);
		assertTrue(stdout.contains("\"oldRaw\":\"/materials/materials.2\""), stdout);
		JsonTestUtil.assertValidJson(stdout);
	}

	@Test
	void insertJsonIncludesReferenceRewrites(@TempDir final Path workspace) throws Exception
	{
		writeInsertModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new InsertRunner().run(context,
												"ModelA.lm",
												"/materials/materials.1",
												"(Material name=Sand)",
												new InsertRunner.Options(true));
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString().trim();
		assertTrue(stdout.contains("\"command\":\"insert\""), stdout);
		assertTrue(stdout.contains("\"referenceRewrites\""), stdout);
		assertTrue(stdout.contains("\"oldRaw\":\"/materials/materials.1\""), stdout);
		JsonTestUtil.assertValidJson(stdout);
	}

	private static void writeRemoveModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=RemoveMeta
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
			(Root domain=test.model name=ModelA metamodels=test.model.RemoveMeta
				mainMaterial=/materials/materials.1
				altMaterial=/materials/materials.2
				(MaterialsEnv
					(Material name=Lava)
					(Material name=Dirt)
					(Material name=Stone)))
			""";

		final var modelB = """
			(Root domain=test.model name=ModelB metamodels=test.model.RemoveMeta imports=test.model.ModelA
				altMaterial=#ModelA/materials/materials.2
				(MaterialsEnv))
			""";

		Files.writeString(workspace.resolve("RemoveMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), modelA);
		Files.writeString(workspace.resolve("ModelB.lm"), modelB);
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
}

