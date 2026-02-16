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

final class BatchModelQualifiedNameRenameTest
{
	@Test
	void batchRenameOfModelHeaderNameAllowsFinalValidation(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("WrapperSerialization.lm"), """
			(MetaModel domain=test.model name=WrapperSerialization
				(JavaWrapper name=Duration qualifiedClassName=java.time.Duration
					(serializer create="if(it!=null){return java.time.Duration.parse(it);}return java.time.Duration.ZERO;"
						convert="if(it!=null){return it.toString();}return \\"PT0S\\";"))

				(Definition WrapperApp
					(includes #LMCore@Model)
					(+att name=duration datatype=@Duration [1..1]))
			)
			""");

		Files.writeString(workspace.resolve("WrapperSerializationApp.lm"), """
			(WrapperApp domain=test.model name=WrapperSerializationApp metamodels=test.model.WrapperSerialization duration="PT20S")
			""");

		final var script = """
			{"cmd":"rename","args":["WrapperSerializationApp.lm","@WrapperSerializationApp","WrapperSerializationAppRenamed"]}
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

		final var updated = Files.readString(workspace.resolve("WrapperSerializationApp.lm"));
		assertTrue(updated.contains("name=WrapperSerializationAppRenamed"), updated);
	}
}

