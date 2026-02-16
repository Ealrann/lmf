package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.ref.RefRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RefRunnerTest
{
	@Test
	void refFindsExactMatchesAcrossImporters(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new RefRunner().run(context,
											 "ModelA.lm",
											 "@Lava",
											 new RefRunner.Options(false, false));

		assertEquals(ExitCodes.OK, exit);
		final var output = outBuffer.toString();
		final var err = errBuffer.toString();
		assertTrue(output.contains("@Lava"), "output:\n" + output + "\nerr:\n" + err);
		assertTrue(output.contains("/materials/materials.0"), "output:\n" + output + "\nerr:\n" + err);
		assertTrue(output.contains("#ModelA/materials/materials.0"), "output:\n" + output + "\nerr:\n" + err);
		assertTrue(output.contains("ModelB.lm"), "output:\n" + output + "\nerr:\n" + err);
	}

	@Test
	void refDescendantsOnlyIncludesPathLikeReferences(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new RefRunner().run(context,
											 "ModelA.lm",
											 "/materials",
											 new RefRunner.Options(true, false));

		assertEquals(ExitCodes.OK, exit);
		final var output = outBuffer.toString();
		final var err = errBuffer.toString();
		assertTrue(output.contains("/materials/materials.0"), "output:\n" + output + "\nerr:\n" + err);
		assertTrue(output.contains("#ModelA/materials/materials.0"), "output:\n" + output + "\nerr:\n" + err);
		assertFalse(output.contains("\t@Lava\t"), "output:\n" + output + "\nerr:\n" + err);
	}

	@Test
	void refFallsBackToDescendantsWhenNoExactMatches(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new RefRunner().run(context,
											 "ModelA.lm",
											 "/materials",
											 new RefRunner.Options(false, false));

		assertEquals(ExitCodes.OK, exit);
		final var output = outBuffer.toString();
		final var err = errBuffer.toString();
		assertTrue(output.contains("/materials/materials.0"), "output:\n" + output + "\nerr:\n" + err);
		assertTrue(output.contains("#ModelA/materials/materials.0"), "output:\n" + output + "\nerr:\n" + err);
		assertFalse(output.contains("\t@Lava\t"), "output:\n" + output + "\nerr:\n" + err);
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
					(+refers mainMaterial [0..1] @Material)
					(+refers altMaterial [0..1] @Material)))
			""";

		final var modelA = """
			(Root domain=test.model name=ModelA metamodels=test.model.RefMeta
				mainMaterial=@Lava altMaterial=/materials/materials.0
				(MaterialsEnv
					(Material name=Lava)
					(Material name=Dirt)))
			""";

		final var modelB = """
			(Root domain=test.model name=ModelB metamodels=test.model.RefMeta imports=test.model.ModelA
				altMaterial=#ModelA/materials/materials.0
				(MaterialsEnv))
			""";

		Files.writeString(workspace.resolve("RefMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), modelA);
		Files.writeString(workspace.resolve("ModelB.lm"), modelB);
	}
}
