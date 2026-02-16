package org.logoce.lmf.core.loader.linking;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;

import static org.junit.jupiter.api.Assertions.*;

public final class ReferenceDiagnosticTest
{
	@Test
	void unresolvedNamedReference_reportsExpectedConceptAndHighlightsValueToken()
	{
		final var metaSource = """
			(MetaModel domain=test.diag name=RefMeta
				(Definition Car (includes group=#LMCore@Named))
				(Definition Person
					(includes group=#LMCore@Named)
					(+refers car [0..1] @Car))
				(Definition CarCompany
					(includes group=#LMCore@Model)
					(+contains ceo @Person [0..1])))
			""";

		final var baseRegistry = ModelRegistry.empty();
		final var metaDoc = new LmLoader(baseRegistry).loadModel(metaSource);
		assertNotNull(metaDoc.model(), "meta model should load");
		assertTrue(metaDoc.diagnostics().stream().noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
				   "meta diagnostics:\n" + metaDoc.diagnostics());

		final var registryBuilder = new ModelRegistry.Builder(baseRegistry);
		registryBuilder.register(metaDoc.model());
		final var registry = registryBuilder.build();

		final var modelSource = """
			(CarCompany domain=test.diag name=BadRef metamodels=test.diag.RefMeta
				(ceo name=Boss car=@doesNotExist))
			""";

		final var modelDoc = new LmLoader(registry).loadModel(modelSource);
		final var errors = modelDoc.diagnostics()
								   .stream()
								   .filter(d -> d.severity() == LmDiagnostic.Severity.ERROR)
								   .toList();
		assertFalse(errors.isEmpty(), "expected errors, got:\n" + modelDoc.diagnostics());

		final var error = errors.getFirst();
		assertTrue(error.message().contains("Cannot resolve reference '@doesNotExist'"), error.message());
		assertTrue(error.message().contains("relation 'car'"), error.message());
		assertTrue(error.message().contains("expected Car"), error.message());
		assertTrue(error.message().contains("group 'Person'"), error.message());

		final int expectedOffset = modelSource.indexOf("@doesNotExist");
		assertEquals(expectedOffset, error.offset(), error.message());
		assertEquals("@doesNotExist".length(), error.length(), error.message());
	}

	@Test
	void mismatchedNamedReference_reportsExpectedAndActualConceptAndHighlightsValueToken()
	{
		final var metaSource = """
			(MetaModel domain=test.diag name=RefMeta
				(Definition Car (includes group=#LMCore@Named))
				(Definition Person
					(includes group=#LMCore@Named)
					(+refers car [0..1] @Car))
				(Definition CarCompany
					(includes group=#LMCore@Model)
					(+contains ceo @Person [0..1])))
			""";

		final var baseRegistry = ModelRegistry.empty();
		final var metaDoc = new LmLoader(baseRegistry).loadModel(metaSource);
		assertNotNull(metaDoc.model(), "meta model should load");
		assertTrue(metaDoc.diagnostics().stream().noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
				   "meta diagnostics:\n" + metaDoc.diagnostics());

		final var registryBuilder = new ModelRegistry.Builder(baseRegistry);
		registryBuilder.register(metaDoc.model());
		final var registry = registryBuilder.build();

		final var modelSource = """
			(CarCompany domain=test.diag name=TypeMismatch metamodels=test.diag.RefMeta
				(ceo name=Boss car=@Boss))
			""";

		final var modelDoc = new LmLoader(registry).loadModel(modelSource);
		final var errors = modelDoc.diagnostics()
								   .stream()
								   .filter(d -> d.severity() == LmDiagnostic.Severity.ERROR)
								   .toList();
		assertFalse(errors.isEmpty(), "expected errors, got:\n" + modelDoc.diagnostics());

		final var error = errors.getFirst();
		assertTrue(error.message().contains("Cannot resolve reference '@Boss'"), error.message());
		assertTrue(error.message().contains("relation 'car'"), error.message());
		assertTrue(error.message().contains("expected Car but found Person"), error.message());
		assertTrue(error.message().contains("group 'Person'"), error.message());

		final int expectedOffset = modelSource.indexOf("@Boss");
		assertEquals(expectedOffset, error.offset(), error.message());
		assertEquals("@Boss".length(), error.length(), error.message());
	}

	@Test
	void ambiguousNamedPath_reportsMatchCountAndHighlightsValueToken()
	{
		final var metaSource = """
			(MetaModel domain=test.diag name=AmbigMeta
				(Definition Car (includes group=#LMCore@Named))
				(Definition Garage
					(includes group=#LMCore@Named)
					(+contains cars @Car [0..*]))
				(Definition Person
					(includes group=#LMCore@Named)
					(+refers car [0..1] @Car))
				(Definition Root
					(includes group=#LMCore@Model)
					(+contains garages @Garage [0..*])
					(+contains ceo @Person [0..1])))
			""";

		final var baseRegistry = ModelRegistry.empty();
		final var metaDoc = new LmLoader(baseRegistry).loadModel(metaSource);
		assertNotNull(metaDoc.model(), "meta model should load");
		assertTrue(metaDoc.diagnostics().stream().noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
				   "meta diagnostics:\n" + metaDoc.diagnostics());

		final var registryBuilder = new ModelRegistry.Builder(baseRegistry);
		registryBuilder.register(metaDoc.model());
		final var registry = registryBuilder.build();

		final var modelSource = """
			(Root domain=test.diag name=Ambiguous metamodels=test.diag.AmbigMeta
				(garages name=G (cars name=Car1))
				(garages name=G (cars name=Car2))
				(ceo name=Boss car=@G/cars.0))
			""";

		final var modelDoc = new LmLoader(registry).loadModel(modelSource);
		final var errors = modelDoc.diagnostics()
								   .stream()
								   .filter(d -> d.severity() == LmDiagnostic.Severity.ERROR)
								   .toList();
		assertFalse(errors.isEmpty(), "expected errors, got:\n" + modelDoc.diagnostics());

		final var error = errors.getFirst();
		assertTrue(error.message().contains("Ambiguous reference '@G/cars.0'"), error.message());
		assertTrue(error.message().contains("relation 'car'"), error.message());
		assertTrue(error.message().contains("expected Car"), error.message());
		assertTrue(error.message().contains("group 'Person'"), error.message());
		assertTrue(error.message().contains("2 matches"), error.message());

		final int expectedOffset = modelSource.indexOf("@G/cars.0");
		assertEquals(expectedOffset, error.offset(), error.message());
		assertEquals("@G/cars.0".length(), error.length(), error.message());
	}
}
