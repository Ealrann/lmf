package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.remove.RemoveRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RemoveRunnerTest
{
	@Test
	void removeUpdatesImportersAndUnsetsReferences(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);
		final var fileA = workspace.resolve("ModelA.lm");
		final var fileB = workspace.resolve("ModelB.lm");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new RemoveRunner().run(context,
												"ModelA.lm",
												"/materials/materials.1");

		assertEquals(ExitCodes.OK, exit);

		final var updatedA = Files.readString(fileA);
		final var updatedB = Files.readString(fileB);

		assertFalse(updatedA.contains("Material name=Dirt"), "updatedA:\n" + updatedA);
		assertFalse(updatedA.contains("mainMaterial="), "updatedA:\n" + updatedA);
		assertTrue(updatedA.contains("altMaterial=/materials/materials.1"), "updatedA:\n" + updatedA);
		assertTrue(updatedB.contains("altMaterial=#ModelA/materials/materials.1"), "updatedB:\n" + updatedB);

		final var output = outBuffer.toString();
		assertTrue(output.contains("\tunset"), "output:\n" + output);
		assertTrue(output.contains("OK: removed"), "output:\n" + output);
	}

	private static void writeModels(final Path workspace) throws Exception
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
}
