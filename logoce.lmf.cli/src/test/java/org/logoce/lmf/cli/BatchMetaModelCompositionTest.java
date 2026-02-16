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

final class BatchMetaModelCompositionTest
{
	@Test
	void batchComposesMetaModelEditsAcrossOperations(@TempDir final Path workspace) throws Exception
	{
		writeMetaModels(workspace);

		final var script = """
			{"cmd":"rename","args":["GraphCore.lm","@ColoredNode","ColorNode"]}
			{"cmd":"move","args":["GraphCore.lm","@ColorNode","/groups.0"]}
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
											null,
											false);

		final var exit = new BatchRunner().run(context, options, new StringReader(script));
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer + "\nout:\n" + outBuffer);

		final var graphCore = Files.readString(workspace.resolve("GraphCore.lm"));
		final var graphExt = Files.readString(workspace.resolve("GraphExt.lm"));

		assertTrue(graphCore.contains("Group ColorNode"), "GraphCore:\n" + graphCore);
		assertTrue(graphExt.contains("#GraphCore@ColorNode"), "GraphExt:\n" + graphExt);
	}

	private static void writeMetaModels(final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("GraphCore.lm"), """
			(MetaModel domain=test.graph name=GraphCore
				(Group ColoredNode (includes group=#LMCore@Named))
				(Group OtherNode (includes group=#LMCore@Named)))
			""");

		Files.writeString(workspace.resolve("GraphExt.lm"), """
			(MetaModel domain=test.graph name=GraphExt imports=test.graph.GraphCore
				(Group ExtNode (includes group=#GraphCore@ColoredNode)))
			""");
	}
}
