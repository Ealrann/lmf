package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.insert.InsertRunner;
import org.logoce.lmf.cli.remove.RemoveRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MetaModelStructuralEditsTest
{
	@Test
	void removeWorksOnMetaModelsWithLowercaseNodes(@TempDir final Path workspace) throws Exception
	{
		copyTestModel(workspace, "logoce.lmf.core.generator/src/test/model/GraphCore.lm", "GraphCore.lm");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new RemoveRunner().run(context, "GraphCore.lm", "@ColoredNode");
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var updated = Files.readString(workspace.resolve("GraphCore.lm"));
		assertFalse(updated.contains("(Definition ColoredNode"), "updated:\n" + updated);
		assertTrue(updated.contains("(Group Node"), "updated:\n" + updated);
		assertTrue(updated.contains("(Enum Color"), "updated:\n" + updated);
	}

	@Test
	void insertWorksOnMetaModelsWithLowercaseNodes(@TempDir final Path workspace) throws Exception
	{
		copyTestModel(workspace, "logoce.lmf.core.generator/src/test/model/GraphCore.lm", "GraphCore.lm");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new InsertRunner().run(context,
												"GraphCore.lm",
												"/enums.0",
												"(Enum Shape Circle,Square)");
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var updated = Files.readString(workspace.resolve("GraphCore.lm"));
		assertTrue(updated.contains("(Enum Shape"), "updated:\n" + updated);
		assertTrue(updated.contains("(Enum Color"), "updated:\n" + updated);
	}

	private static void copyTestModel(final Path workspace, final String sourceRelativeToRepoRoot, final String targetFileName) throws Exception
	{
		final var repoRoot = findRepoRoot();
		final var source = repoRoot.resolve(sourceRelativeToRepoRoot);
		Files.writeString(workspace.resolve(targetFileName), Files.readString(source));
	}

	private static Path findRepoRoot() throws Exception
	{
		Path cursor = Path.of("").toAbsolutePath().normalize();
		while (cursor != null)
		{
			if (Files.exists(cursor.resolve("settings.gradle")))
			{
				return cursor;
			}
			cursor = cursor.getParent();
		}
		throw new IllegalStateException("Cannot locate repo root (settings.gradle not found)");
	}
}
