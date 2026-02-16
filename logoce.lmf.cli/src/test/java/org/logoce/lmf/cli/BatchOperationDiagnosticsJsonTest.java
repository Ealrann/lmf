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

final class BatchOperationDiagnosticsJsonTest
{
	@Test
	void batchJsonIncludesDiagnosticsOnValidationFailure(@TempDir final Path workspace) throws Exception
	{
		writeMetaModels(workspace);

		final var script = """
			{"cmd":"remove-value","args":["/","imports","test.model.Types"]}
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
											"Action.lm",
											true);

		final var exit = new BatchRunner().run(context, options, new StringReader(script));
		assertEquals(ExitCodes.INVALID, exit, "err:\n" + errBuffer + "\nout:\n" + outBuffer);

		final var stdout = outBuffer.toString().trim();
		JsonTestUtil.assertValidJson(stdout);
		assertTrue(stdout.contains("\"diagnostics\""), stdout);
		assertTrue(stdout.contains("Cannot resolve"), stdout);
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

