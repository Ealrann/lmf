package org.logoce.lmf.cli.edit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.format.LmSourceFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WorkspaceEditPipelineTest
{
	@Test
	void pipelineFormatsBeforeValidation(@TempDir final Path workspace)
	{
		final var source = "(Root domain=test.model name=ModelA metamodels=test.model.BatchMeta (MaterialsEnv (Material name=Lava)))";
		final var formatter = new LmSourceFormatter();
		final var expected = formatter.formatOrOriginal(source);
		final var path = workspace.resolve("ModelA.lm");
		final var writer = new StringWriter();

		final var validated = new AtomicBoolean(false);
		final var validationContext = new CustomValidationContext((sources, err) ->
		{
			validated.set(true);
			assertEquals(expected, sources.get(path));
			return true;
		});

		final var pipeline = new WorkspaceEditPipeline();
		final var outcome = pipeline.processSources(validationContext,
										   Map.of(path, source),
										   new EditOptions(true, true, false, false),
										   new PrintWriter(writer));

		assertTrue(validated.get());
		assertTrue(outcome.validationPassed());
		assertFalse(outcome.wrote());
		assertEquals(expected, outcome.sources().get(path));
	}

	@Test
	void pipelineSkipsWriteOnValidationFailure(@TempDir final Path workspace)
	{
		final var source = "(Root domain=test.model name=ModelA metamodels=test.model.BatchMeta (MaterialsEnv))";
		final var path = workspace.resolve("ModelA.lm");
		final var writer = new StringWriter();

		final var validationContext = new CustomValidationContext((sources, err) -> false);
		final var pipeline = new WorkspaceEditPipeline();
		final var outcome = pipeline.processSources(validationContext,
										   Map.of(path, source),
										   new EditOptions(true, true, false, true),
										   new PrintWriter(writer));

		assertFalse(outcome.validationPassed());
		assertFalse(outcome.wrote());
	}
}
