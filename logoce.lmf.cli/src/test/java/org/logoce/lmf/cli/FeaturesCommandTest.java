package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FeaturesCommandTest
{
	@Test
	void featuresJsonShowsContainmentVsReferenceKinds(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);

		final var outBuffer = new ByteArrayOutputStream();
		final var errBuffer = new ByteArrayOutputStream();
		final var cli = new LmCli(new PrintStream(outBuffer), new PrintStream(errBuffer), workspace);

		final var exitOuter = cli.run(new String[] { "features", "ModelB.lm", "/barrier", "--json" });
		assertEquals(ExitCodes.OK, exitOuter, "err:\n" + errBuffer);
		final var outerJson = outBuffer.toString(StandardCharsets.UTF_8).trim();
		JsonTestUtil.assertValidJson(outerJson);
		assertTrue(outerJson.contains("\"command\":\"features\""), outerJson);
		assertTrue(outerJson.contains("\"ref\":\"/barrier\""), outerJson);
		assertTrue(outerJson.contains("\"name\":\"buffers\""), outerJson);
		assertTrue(outerJson.contains("\"kind\":\"CONTAINS\""), outerJson);
		assertTrue(outerJson.contains("\"type\":\"BufferReference\""), outerJson);

		outBuffer.reset();
		errBuffer.reset();

		final var exitInner = cli.run(new String[] { "features", "ModelB.lm", "/barrier/buffers", "--json" });
		assertEquals(ExitCodes.OK, exitInner, "err:\n" + errBuffer);
		final var innerJson = outBuffer.toString(StandardCharsets.UTF_8).trim();
		JsonTestUtil.assertValidJson(innerJson);
		assertTrue(innerJson.contains("\"ref\":\"/barrier/buffers\""), innerJson);
		assertTrue(innerJson.contains("\"name\":\"buffers\""), innerJson);
		assertTrue(innerJson.contains("\"kind\":\"REFERS\""), innerJson);
		assertTrue(innerJson.contains("\"type\":\"Buffer\""), innerJson);
	}

	private static void writeModels(final Path workspace) throws Exception
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

