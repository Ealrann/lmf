package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.assign.SetRunner;
import org.logoce.lmf.cli.assign.UnsetRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SetUnsetRunnerTest
{
	@Test
	void setInsertsAssignmentWhenMissingAndUnsetRemovesIt(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);
		final var fileA = workspace.resolve("ModelA.lm");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var setExit = new SetRunner().run(context,
												"ModelA.lm",
												"/",
												"mainMaterial",
												"/materials/materials.1");

		assertEquals(ExitCodes.OK, setExit, "err:\n" + errBuffer);
		final var updatedA = Files.readString(fileA);
		assertTrue(updatedA.contains("mainMaterial=/materials/materials.1"), "updated:\n" + updatedA);

		final var secondOut = new StringWriter();
		final var secondErr = new StringWriter();
		final var secondContext = new CliContext(workspace, new PrintWriter(secondOut), new PrintWriter(secondErr));

		final var unsetExit = new UnsetRunner().run(secondContext,
													"ModelA.lm",
													"/",
													"mainMaterial");

		assertEquals(ExitCodes.OK, unsetExit, "err:\n" + secondErr);
		final var afterUnset = Files.readString(fileA);
		assertFalse(afterUnset.contains("mainMaterial="), "updated:\n" + afterUnset);
	}

	@Test
	void setUpdatesExistingAttribute(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);
		final var fileA = workspace.resolve("ModelA.lm");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new SetRunner().run(context,
											  "ModelA.lm",
											  "/materials/materials.1",
											  "name",
											  "Sand");

		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);
		final var updatedA = Files.readString(fileA);
		assertTrue(updatedA.contains("Material name=Sand"), "updated:\n" + updatedA);
		assertFalse(updatedA.contains("Material name=Dirt"), "updated:\n" + updatedA);
	}

	private static void writeModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=AssignMeta
				(Group Named (includes group=#LMCore@Named))
				(Definition Material (includes group=@Named))
				(Definition MaterialsEnv (includes group=#LMCore@LMObject)
					(+contains materials [0..*] @Material))
				(Definition Root (includes group=#LMCore@Model)
					(+contains materials [1..1] @MaterialsEnv)
					(+refers mainMaterial [0..1] @Material)))
			""";

		final var modelA = """
			(Root domain=test.model name=ModelA metamodels=test.model.AssignMeta
				(MaterialsEnv
					(Material name=Lava)
					(Material name=Dirt)
					(Material name=Stone)))
			""";

		Files.writeString(workspace.resolve("AssignMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), modelA);
	}
}

