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

final class RefCommandJsonTest
{
	@Test
	void refJsonOutputsValidJson(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);

		final var outBuffer = new ByteArrayOutputStream();
		final var errBuffer = new ByteArrayOutputStream();
		final var cli = new LmCli(new PrintStream(outBuffer), new PrintStream(errBuffer), workspace);

		final var exit = cli.run(new String[] { "ref", "ModelA.lm", "@Lava", "--json" });
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString(StandardCharsets.UTF_8).trim();
		assertTrue(stdout.contains("\"command\":\"ref\""), stdout);
		assertTrue(stdout.contains("\"matches\""), stdout);
		JsonTestUtil.assertValidJson(stdout);
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

