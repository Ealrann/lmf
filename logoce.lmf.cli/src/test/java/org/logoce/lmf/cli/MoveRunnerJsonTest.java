package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.move.MoveRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MoveRunnerJsonTest
{
	@Test
	void moveJsonReportsReferenceRewritesAndPreservesAnchorStyle(@TempDir final Path workspace) throws Exception
	{
		final var repoRoot = findRepoRoot();
		Files.writeString(workspace.resolve("CarCompany.lm"),
						  Files.readString(repoRoot.resolve("logoce.lmf.core.api/src/test/model/CarCompany.lm")));
		Files.writeString(workspace.resolve("PeugeotWithReferencePath.lm"),
						  Files.readString(repoRoot.resolve("logoce.lmf.core.api/src/test/model/PeugeotWithReferencePath.lm")));

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new MoveRunner().run(context,
											  "PeugeotWithReferencePath.lm",
											  "/parcs.0/cars.1",
											  "/parcs.0/cars.0",
											  new MoveRunner.Options(true));
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString().trim();
		assertTrue(stdout.contains("\"command\":\"move\""), stdout);
		assertTrue(stdout.contains("\"referenceRewrites\""), stdout);
		assertTrue(stdout.contains("\"oldRaw\":\"@PeugeotCompanyWithReferencePaths/parcs.0/cars.1\""), stdout);
		assertTrue(stdout.contains("\"newRaw\":\"@PeugeotCompanyWithReferencePaths/parcs.0/cars.0\""), stdout);
		JsonTestUtil.assertValidJson(stdout);

		final var updated = Files.readString(workspace.resolve("PeugeotWithReferencePath.lm"));
		assertTrue(updated.contains("car=@PeugeotCompanyWithReferencePaths/parcs.0/cars.0"), updated);
		assertTrue(updated.contains("car=@peugeot1/../cars.0"), updated);
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

