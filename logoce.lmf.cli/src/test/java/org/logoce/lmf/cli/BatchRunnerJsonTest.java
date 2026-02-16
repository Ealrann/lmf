package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.batch.BatchOptions;
import org.logoce.lmf.cli.batch.BatchRunner;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BatchRunnerJsonTest
{
	@Test
	void batchJsonOutputsValidJson(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);

		final var script = """
			{"cmd":"remove","args":["/materials/materials.1"]}
			{"cmd":"rename","args":["/materials/materials.0","Lava Boiling"]}
			""";

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var options = new BatchOptions(null,
											true,
											false,
											false,
											false,
											BatchOptions.ValidateMode.FINAL,
											"ModelA.lm",
											true);

		final var exit = new BatchRunner().run(context, options, new StringReader(script));
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString().trim();
		assertTrue(stdout.contains("\"command\":\"batch\""), stdout);
		assertTrue(stdout.contains("\"operations\""), stdout);
		assertTrue(stdout.contains("\"finalization\""), stdout);
		assertTrue(stdout.contains("\"plannedFiles\""), stdout);
		assertTrue(stdout.contains("\"writtenFiles\""), stdout);
		JsonTestUtil.assertValidJson(stdout);
	}

	private static void writeModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=BatchMeta
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
			(Root domain=test.model name=ModelA metamodels=test.model.BatchMeta
				mainMaterial=/materials/materials.1
				altMaterial=/materials/materials.2
				(MaterialsEnv
					(Material name=Lava)
					(Material name=Dirt)
					(Material name=Stone)))
			""";

		Files.writeString(workspace.resolve("BatchMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), modelA);
	}
}
