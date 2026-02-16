package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.assign.ClearRunner;
import org.logoce.lmf.cli.assign.RemoveValueRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MetaModelImportsEditValidationTest
{
	@Test
	void removeValueOnImportsRefusesToWriteWhenImportIsStillUsed(@TempDir final Path workspace) throws Exception
	{
		writeMetaModels(workspace);
		final var actionFile = workspace.resolve("Action.lm");
		final var before = Files.readString(actionFile);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new RemoveValueRunner().run(context,
													 "Action.lm",
													 "/",
													 "imports",
													 "test.model.Types",
													 new RemoveValueRunner.Options(true));

		assertEquals(ExitCodes.INVALID, exit, "err:\n" + errBuffer + "\nout:\n" + outBuffer);
		assertEquals(before, Files.readString(actionFile), "file should not be modified when validation fails");
		assertTrue(errBuffer.toString().contains("Cannot resolve imported model") ||
					   errBuffer.toString().contains("Cannot resolve model 'Types'"),
				   "stderr:\n" + errBuffer);
		assertTrue(outBuffer.toString().contains("\"diagnostics\""), "stdout:\n" + outBuffer);
		assertTrue(outBuffer.toString().contains("Cannot resolve"), "stdout:\n" + outBuffer);
	}

	@Test
	void clearOnImportsRefusesToWriteWhenImportIsStillUsed(@TempDir final Path workspace) throws Exception
	{
		writeMetaModels(workspace);
		final var actionFile = workspace.resolve("Action.lm");
		final var before = Files.readString(actionFile);

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new ClearRunner().run(context,
											   "Action.lm",
											   "/",
											   "imports",
											   new ClearRunner.Options(true));

		assertEquals(ExitCodes.INVALID, exit, "err:\n" + errBuffer + "\nout:\n" + outBuffer);
		assertEquals(before, Files.readString(actionFile), "file should not be modified when validation fails");
		assertTrue(errBuffer.toString().contains("Cannot resolve imported model") ||
					   errBuffer.toString().contains("Cannot resolve model 'Types'"),
				   "stderr:\n" + errBuffer);
		assertTrue(outBuffer.toString().contains("\"diagnostics\""), "stdout:\n" + outBuffer);
		assertTrue(outBuffer.toString().contains("Cannot resolve"), "stdout:\n" + outBuffer);
	}

	private static void writeMetaModels(final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("Types.lm"), """
			(MetaModel domain=test.model name=Types
				(Group Type
					(includes group=#LMCore@LMObject)))
			""");

		Files.writeString(workspace.resolve("Action.lm"), """
			(MetaModel domain=test.model name=Action imports=test.model.Types
				(Definition Action
					(includes group=#Types@Type)))
			""");
	}
}
