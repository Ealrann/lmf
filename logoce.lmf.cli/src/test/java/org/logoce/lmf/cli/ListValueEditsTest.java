package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.assign.AddRunner;
import org.logoce.lmf.cli.assign.ClearRunner;
import org.logoce.lmf.cli.assign.RemoveValueRunner;
import org.logoce.lmf.cli.assign.SetRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ListValueEditsTest
{
	@Test
	void listValueCommandsWorkForAttributesAndNonContainmentRelations(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);
		final var file = workspace.resolve("ModelA.lm");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var setExit = new SetRunner().run(context,
												"ModelA.lm",
												"/",
												"selectedMaterials",
												"/materials/materials.0,/materials/materials.2");
		assertEquals(ExitCodes.OK, setExit, "err:\n" + errBuffer);
		assertTrue(Files.readString(file).contains("selectedMaterials=/materials/materials.0,/materials/materials.2"));

		final var addExit = new AddRunner().run(context,
												"ModelA.lm",
												"/",
												"selectedMaterials",
												"/materials/materials.1");
		assertEquals(ExitCodes.OK, addExit, "err:\n" + errBuffer);
		assertTrue(Files.readString(file).contains("selectedMaterials=/materials/materials.0,/materials/materials.2,/materials/materials.1"));

		final var beforeIdempotent = Files.readString(file);
		final var secondAddExit = new AddRunner().run(context,
													  "ModelA.lm",
													  "/",
													  "selectedMaterials",
													  "/materials/materials.1");
		assertEquals(ExitCodes.OK, secondAddExit, "err:\n" + errBuffer);
		assertEquals(beforeIdempotent, Files.readString(file));

		final var removeExit = new RemoveValueRunner().run(context,
														   "ModelA.lm",
														   "/",
														   "selectedMaterials",
														   "/materials/materials.2");
		assertEquals(ExitCodes.OK, removeExit, "err:\n" + errBuffer);
		final var afterRemove = Files.readString(file);
		assertTrue(afterRemove.contains("selectedMaterials=/materials/materials.0,/materials/materials.1"));
		assertFalse(afterRemove.contains("selectedMaterials=/materials/materials.0,/materials/materials.2"));

		final var clearExit = new ClearRunner().run(context,
													"ModelA.lm",
													"/",
													"selectedMaterials");
		assertEquals(ExitCodes.OK, clearExit, "err:\n" + errBuffer);
		assertFalse(Files.readString(file).contains("selectedMaterials="));

		final var tagsSetExit = new SetRunner().run(context,
													"ModelA.lm",
													"/",
													"tags",
													"alpha,beta");
		assertEquals(ExitCodes.OK, tagsSetExit, "err:\n" + errBuffer);
		assertTrue(Files.readString(file).contains("tags=alpha,beta"));

		final var tagsAddExit = new AddRunner().run(context,
													"ModelA.lm",
													"/",
													"tags",
													"gamma");
		assertEquals(ExitCodes.OK, tagsAddExit, "err:\n" + errBuffer);
		assertTrue(Files.readString(file).contains("tags=alpha,beta,gamma"));
	}

	@Test
	void listValueCommandsSupportWhitespaceInRelationValues(@TempDir final Path workspace) throws Exception
	{
		writeWhitespaceRelationModels(workspace);
		final var file = workspace.resolve("ModelB.lm");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var addExit = new AddRunner().run(context,
												"ModelB.lm",
												"/barrier/buffers",
												"buffers",
												"@Mass Buffer Tmp");
		assertEquals(ExitCodes.OK, addExit, "err:\n" + errBuffer);

		final var afterAdd = Files.readString(file);
		assertTrue(afterAdd.contains("buffers=\"@Mass Buffer 1\",\"@Mass Buffer 2\",\"@Mass Buffer Tmp\""), afterAdd);

		final var removeExit = new RemoveValueRunner().run(context,
														   "ModelB.lm",
														   "/barrier/buffers",
														   "buffers",
														   "@Mass Buffer 2");
		assertEquals(ExitCodes.OK, removeExit, "err:\n" + errBuffer);

		final var afterRemove = Files.readString(file);
		assertFalse(afterRemove.contains("@Mass Buffer 2"), afterRemove);
		assertTrue(afterRemove.contains("buffers=\"@Mass Buffer 1\",\"@Mass Buffer Tmp\""), afterRemove);

		final var setExit = new SetRunner().run(context,
												"ModelB.lm",
												"/barrier/buffers",
												"buffers",
												"@Mass Buffer 2,@Mass Buffer Tmp");
		assertEquals(ExitCodes.OK, setExit, "err:\n" + errBuffer);

		final var afterSet = Files.readString(file);
		assertTrue(afterSet.contains("buffers=\"@Mass Buffer 2\",\"@Mass Buffer Tmp\""), afterSet);
	}

	@Test
	void setRejectsContainmentAndSuggestsStructuralCommands(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new SetRunner().run(context,
											 "ModelA.lm",
											 "/",
											 "materials",
											 "/materials");

		assertEquals(ExitCodes.INVALID, exit);
		assertTrue(errBuffer.toString().contains("use insert/move/remove/replace"), errBuffer.toString());
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

	private static void writeWhitespaceRelationModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=WhitespaceRefMeta
				(Group Named (includes group=#LMCore@Named))
				(Definition Buffer (includes group=@Named))
				(Definition BufferReference (includes group=#LMCore@LMObject)
					(+refers buffers [0..*] @Buffer))
				(Definition BufferBarrier (includes group=#LMCore@LMObject)
					(+contains buffers [0..1] @BufferReference))
				(Definition Root (includes group=#LMCore@Model)
					(+contains barrier [0..1] @BufferBarrier)
					(+contains buffers [0..*] @Buffer)))
			""";

		final var model = """
			(Root domain=test.model name=ModelB metamodels=test.model.WhitespaceRefMeta
				(Buffer name="Mass Buffer 1")
				(Buffer name="Mass Buffer 2")
				(Buffer name="Mass Buffer Tmp")
				(BufferBarrier
					(BufferReference buffers="@Mass Buffer 1","@Mass Buffer 2")))
			""";

		Files.writeString(workspace.resolve("WhitespaceRefMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelB.lm"), model);
	}
}
