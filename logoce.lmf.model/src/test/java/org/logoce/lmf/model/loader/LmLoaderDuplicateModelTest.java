package org.logoce.lmf.model.loader;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.LMCoreModelPackage;
import org.logoce.lmf.model.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.model.util.ModelRegistry;
import test.model.carcompany.CarCompanyModelPackage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LmLoaderDuplicateModelTest
{
	@Test
	void loadModel_refusesWhenQualifiedNameAlreadyInRegistry() throws Exception
	{
		final var registryBuilder = new ModelRegistry.Builder();
		registryBuilder.register(LMCoreModelPackage.MODEL);
		registryBuilder.register(CarCompanyModelPackage.MODEL);
		final var registry = registryBuilder.build();

		final var loader = new LmLoader(registry);
		final var source = Files.readString(Path.of("src/test/model/CarCompany.lm"), StandardCharsets.UTF_8);
		final var document = loader.loadModel(source);

		assertNull(document.model(), "Loader should refuse to load a model already present in the registry");
		assertNotNull(document.diagnostics(), "Diagnostics should be present");
		assertTrue(document.diagnostics()
						   .stream()
						   .anyMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR &&
										  d.message() != null &&
										  d.message().contains("Model already exists in registry")),
				   "Diagnostics should contain a duplicate-model error");
	}
}

