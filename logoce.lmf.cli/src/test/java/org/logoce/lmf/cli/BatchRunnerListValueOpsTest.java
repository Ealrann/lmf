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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BatchRunnerListValueOpsTest
{
	@Test
	void batchSupportsAddRemoveValueAndClear(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);
		final var file = workspace.resolve("ModelA.lm");

		final var script = """
			{"cmd":"set","args":["/","selectedMaterials","/materials/materials.0,/materials/materials.2"]}
			{"cmd":"add","args":["/","selectedMaterials","/materials/materials.1"]}
			{"cmd":"remove-value","args":["/","selectedMaterials","/materials/materials.2"]}
			{"cmd":"set","args":["/","tags","alpha,beta"]}
			{"cmd":"add","args":["/","tags","gamma"]}
			{"cmd":"remove-value","args":["/","tags","beta"]}
			{"cmd":"clear","args":["/","tags"]}
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
											false);

		final var exit = new BatchRunner().run(context, options, new StringReader(script));
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var updated = Files.readString(file);
		assertTrue(updated.contains("selectedMaterials=/materials/materials.0,/materials/materials.1"), updated);
		assertFalse(updated.contains("selectedMaterials=/materials/materials.2"), updated);
		assertFalse(updated.contains("tags="), updated);
	}

	private static void writeModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=AssignListMeta
				(Group Named (includes group=#LMCore@Named))
				(Definition Material (includes group=@Named))
				(Definition MaterialsEnv (includes group=#LMCore@LMObject)
					(+contains materials [0..*] @Material))
				(Definition Root (includes group=#LMCore@Model)
					(+contains materials [1..1] @MaterialsEnv)
					(+refers selectedMaterials [0..*] @Material)
					(+att name=tags datatype=#LMCore@string [0..*])))
			""";

		final var model = """
			(Root domain=test.model name=ModelA metamodels=test.model.AssignListMeta
				(MaterialsEnv
					(Material name=Lava)
					(Material name=Dirt)
					(Material name=Stone)))
			""";

		Files.writeString(workspace.resolve("AssignListMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), model);
	}
}

