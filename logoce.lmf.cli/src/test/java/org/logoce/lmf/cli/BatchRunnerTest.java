package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.batch.BatchOptions;
import org.logoce.lmf.cli.batch.BatchRunner;
import org.logoce.lmf.cli.format.LmSourceFormatter;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BatchRunnerTest
{
	@Test
	void batchAppliesOperationsAndWritesOnce(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);
		final var fileA = workspace.resolve("ModelA.lm");
		final var fileB = workspace.resolve("ModelB.lm");

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
											"ModelA.lm");

		final var exit = new BatchRunner().run(context, options, new StringReader(script));
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var updatedA = Files.readString(fileA);
		final var updatedB = Files.readString(fileB);

		assertFalse(updatedA.contains("Material name=Dirt"), "updatedA:\n" + updatedA);
		assertTrue(updatedA.contains("Material name=\"Lava Boiling\""), "updatedA:\n" + updatedA);
		assertFalse(updatedA.contains("mainMaterial="), "updatedA:\n" + updatedA);
		assertTrue(updatedA.contains("altMaterial=/materials/materials.1"), "updatedA:\n" + updatedA);
		assertTrue(updatedB.contains("altMaterial=#ModelA/materials/materials.1"), "updatedB:\n" + updatedB);
	}

	@Test
	void dryRunDoesNotWriteFiles(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);
		final var fileA = workspace.resolve("ModelA.lm");
		final var originalA = Files.readString(fileA);

		final var script = """
			{"cmd":"rename","args":["ModelA.lm","/materials/materials.0","Lava Boiling"]}
			""";

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var options = new BatchOptions(null,
											true,
											true,
											false,
											false,
											BatchOptions.ValidateMode.FINAL,
											null);

		final var exit = new BatchRunner().run(context, options, new StringReader(script));
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var afterA = Files.readString(fileA);
		assertEquals(originalA, afterA);
	}

	@Test
	void batchValidateEachFormatsStagedSources(@TempDir final Path workspace) throws Exception
	{
		writeUnformattedModels(workspace);
		final var fileA = workspace.resolve("ModelA.lm");
		final var originalA = Files.readString(fileA);
		final var formatter = new LmSourceFormatter();

		assertNotEquals(formatter.formatOrOriginal(originalA), originalA);

		final var script = """
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
											BatchOptions.ValidateMode.EACH,
											"ModelA.lm");

		final var exit = new BatchRunner().run(context, options, new StringReader(script));
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var afterA = Files.readString(fileA);
		assertEquals(afterA, formatter.formatOrOriginal(afterA));
	}

	@Test
	void batchValidateFinalFormatsStagedSources(@TempDir final Path workspace) throws Exception
	{
		writeUnformattedModels(workspace);
		final var fileA = workspace.resolve("ModelA.lm");
		final var originalA = Files.readString(fileA);
		final var formatter = new LmSourceFormatter();

		assertNotEquals(formatter.formatOrOriginal(originalA), originalA);

		final var script = """
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
											"ModelA.lm");

		final var exit = new BatchRunner().run(context, options, new StringReader(script));
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var afterA = Files.readString(fileA);
		assertEquals(afterA, formatter.formatOrOriginal(afterA));
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

		final var modelB = """
			(Root domain=test.model name=ModelB metamodels=test.model.BatchMeta imports=test.model.ModelA
				altMaterial=#ModelA/materials/materials.2
				(MaterialsEnv))
			""";

		Files.writeString(workspace.resolve("BatchMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), modelA);
		Files.writeString(workspace.resolve("ModelB.lm"), modelB);
	}

	private static void writeUnformattedModels(final Path workspace) throws Exception
	{
		final var meta = "(MetaModel domain=test.model name=BatchMeta (Group Named (includes group=#LMCore@Named))" +
						 " (Definition Material (includes group=@Named))" +
						 " (Definition MaterialsEnv (includes group=#LMCore@LMObject) (+contains materials [0..*] @Material))" +
						 " (Definition Root (includes group=#LMCore@Model) (+contains materials [1..1] @MaterialsEnv)" +
						 " (+refers mainMaterial [0..1] @Material) (+refers altMaterial [0..1] @Material)))";

		final var modelA = "(Root domain=test.model name=ModelA metamodels=test.model.BatchMeta " +
						   "mainMaterial=/materials/materials.1 altMaterial=/materials/materials.2 " +
						   "(MaterialsEnv (Material name=Lava) (Material name=Dirt) (Material name=Stone)))";

		Files.writeString(workspace.resolve("BatchMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), modelA);
	}
}
