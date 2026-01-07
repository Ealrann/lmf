package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.rename.RenameRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RenameRunnerTest
{
	@Test
	void renameUpdatesNameAndReferences(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new RenameRunner().run(context, "ModelA.lm", "@Lava", "Lava Boiling");
		assertEquals(ExitCodes.OK, exit);

		final var updatedA = Files.readString(workspace.resolve("ModelA.lm"));
		final var updatedB = Files.readString(workspace.resolve("ModelB.lm"));

		assertTrue(updatedA.contains("(Material \"Lava Boiling\")"), "updatedA:\n" + updatedA);
		assertTrue(updatedA.contains("mainMaterial=\"@Lava Boiling\""), "updatedA:\n" + updatedA);
		assertTrue(updatedB.contains("mainMaterial=\"#ModelA@Lava Boiling\""), "updatedB:\n" + updatedB);
	}

	@Test
	void renameUpdatesAnchoredPathReferences(@TempDir final Path workspace) throws Exception
	{
		writeAnchorModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new RenameRunner().run(context, "ModelA.lm", "@a", "aa");
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var updatedA = Files.readString(workspace.resolve("ModelA.lm"));
		final var updatedB = Files.readString(workspace.resolve("ModelB.lm"));

		assertTrue(updatedA.contains("(Item aa)"), "updatedA:\n" + updatedA);
		assertTrue(updatedA.contains("selected=@aa/../items.1"), "updatedA:\n" + updatedA);
		assertTrue(updatedB.contains("selected=#ModelA@aa/../items.1"), "updatedB:\n" + updatedB);
	}

	private static void writeModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=RenameMeta
				(Group Named (includes group=#LMCore@Named))
				(Definition Material (includes group=@Named))
				(Definition MaterialsEnv (includes group=#LMCore@LMObject)
					(+contains materials [0..*] @Material))
				(Definition Root (includes group=#LMCore@Model)
					(+contains materials [1..1] @MaterialsEnv)
					(+refers mainMaterial [0..1] @Material)))
			""";

		final var modelA = """
			(Root domain=test.model name=ModelA metamodels=test.model.RenameMeta
				mainMaterial=@Lava
				(MaterialsEnv
					(Material Lava)
					(Material Dirt)))
			""";

		final var modelB = """
			(Root domain=test.model name=ModelB metamodels=test.model.RenameMeta imports=test.model.ModelA
				mainMaterial=#ModelA@Lava
				(MaterialsEnv))
			""";

		Files.writeString(workspace.resolve("RenameMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), modelA);
		Files.writeString(workspace.resolve("ModelB.lm"), modelB);
	}

	private static void writeAnchorModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=RenameAnchorMeta
				(Group Named (includes group=#LMCore@Named))
				(Definition Item (includes group=@Named))
				(Definition Root (includes group=#LMCore@Model)
					(+contains items [0..*] @Item)
					(+refers selected [0..1] @Item)))
			""";

		final var modelA = """
			(Root domain=test.model name=ModelA metamodels=test.model.RenameAnchorMeta
				selected=@a/../items.1
				(Item a)
				(Item b))
			""";

		final var modelB = """
			(Root domain=test.model name=ModelB metamodels=test.model.RenameAnchorMeta imports=test.model.ModelA
				selected=#ModelA@a/../items.1
				(Item b))
			""";

		Files.writeString(workspace.resolve("RenameAnchorMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), modelA);
		Files.writeString(workspace.resolve("ModelB.lm"), modelB);
	}
}
