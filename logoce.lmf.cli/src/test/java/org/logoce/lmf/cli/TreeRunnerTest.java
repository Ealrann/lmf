package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.tree.TreeRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TreeRunnerTest
{
	@Test
	void treeRootOutputsAbsolutePaths(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

			final var exit = new TreeRunner().run(context,
										 "ModelA.lm",
										 new TreeRunner.Options(10, "/materials"));
			assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

			final var output = outBuffer.toString();
			assertTrue(output.contains("/materials/materials.0\tMaterial\tLava"), "output:\n" + output);
			assertTrue(output.contains("/materials/materials.1\tMaterial\tDirt"), "output:\n" + output);
			assertFalse(output.lines().anyMatch(line -> line.startsWith("/materials.0\t")), "output:\n" + output);
		}

	private static void writeModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=TreeRootMeta
				(Group Named (includes group=#LMCore@Named))
				(Definition Material (includes group=@Named))
				(Definition MaterialsEnv (includes group=#LMCore@LMObject)
					(+contains materials [0..*] @Material))
				(Definition Root (includes group=#LMCore@Model)
					(+contains materials [1..1] @MaterialsEnv)))
			""";

		final var modelA = """
			(Root domain=test.model name=ModelA metamodels=test.model.TreeRootMeta
				(MaterialsEnv
					(Material name=Lava)
					(Material name=Dirt)))
			""";

		Files.writeString(workspace.resolve("TreeRootMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), modelA);
	}
}
