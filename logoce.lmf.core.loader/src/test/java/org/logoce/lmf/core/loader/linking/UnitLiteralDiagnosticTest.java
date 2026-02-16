package org.logoce.lmf.core.loader.linking;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;

import static org.junit.jupiter.api.Assertions.*;

public final class UnitLiteralDiagnosticTest
{
	@Test
	void invalidFloatLiteral_reportsHelpfulDiagnosticOnValueToken()
	{
		final var metaSource = """
			(MetaModel domain=test.diag name=FloatMeta
				(Definition Root (includes group=#LMCore@Model)
					(+att name=weight datatype=#LMCore@float [0..1])))
			""";

		final var baseRegistry = ModelRegistry.empty();
		final var metaDoc = new LmLoader(baseRegistry).loadModel(metaSource);
		assertNotNull(metaDoc.model());
		assertTrue(metaDoc.diagnostics().stream().noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
				   "meta diagnostics:\n" + metaDoc.diagnostics());

		final var registryBuilder = new ModelRegistry.Builder(baseRegistry);
		registryBuilder.register(metaDoc.model());
		final var registry = registryBuilder.build();

		final var modelSource = """
			(Root domain=test.diag name=BadFloat metamodels=test.diag.FloatMeta weight=70.5)
			""";
		final var modelDoc = new LmLoader(registry).loadModel(modelSource);

		final var errors = modelDoc.diagnostics()
								   .stream()
								   .filter(d -> d.severity() == LmDiagnostic.Severity.ERROR)
								   .toList();
		assertFalse(errors.isEmpty(), "expected errors, got:\n" + modelDoc.diagnostics());

		final var error = errors.getFirst();
		assertTrue(error.message().contains("Invalid float literal"), error.message());
		assertTrue(error.message().contains("f"), error.message());

		final int expectedOffset = modelSource.indexOf("70.5");
		assertEquals(expectedOffset, error.offset(), error.message());
		assertEquals("70.5".length(), error.length(), error.message());
	}
}

