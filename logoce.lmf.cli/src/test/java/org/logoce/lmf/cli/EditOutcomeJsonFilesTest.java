package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.assign.SetRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EditOutcomeJsonFilesTest
{
	@Test
	void outcomeJsonSeparatesPlannedAndWrittenFiles(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("FloatMeta.lm"), """
			(MetaModel domain=test.diag name=FloatMeta
				(Definition Root (includes group=#LMCore@Model)
					(+att name=weight datatype=#LMCore@float [0..1])))
			""");
		Files.writeString(workspace.resolve("ModelA.lm"), """
			(Root domain=test.diag name=ModelA metamodels=test.diag.FloatMeta)
			""");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new SetRunner().run(context,
											"ModelA.lm",
											"/",
											"weight",
											"70.5",
											new SetRunner.Options(true));
		assertEquals(ExitCodes.INVALID, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString().trim();
		JsonTestUtil.assertValidJson(stdout);
		assertTrue(stdout.contains("\"plannedFileCount\":1"), stdout);
		assertTrue(stdout.contains("\"plannedFiles\":[\"ModelA.lm\"]"), stdout);
		assertTrue(stdout.contains("\"writtenFileCount\":0"), stdout);
		assertTrue(stdout.contains("\"writtenFiles\":[]"), stdout);
	}
}

