package org.logoce.lmf.model.loader;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.LMCoreModelPackage;
import org.logoce.lmf.model.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.model.loader.model.LmDocument;
import org.logoce.lmf.model.util.ModelRegistry;
import test.model.carcompany.CarCompany;
import test.model.carcompany.CarCompanyModelPackage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public final class M1LoaderTest
{
	@Test
	void loadPeugeotM1Model_usesCarCompanyAsMetamodel() throws IOException
	{
		final var registryBuilder = new ModelRegistry.Builder();
		registryBuilder.register(LMCoreModelPackage.MODEL);
		registryBuilder.register(CarCompanyModelPackage.MODEL);
		final var registry = registryBuilder.build();

		final var loader = new LmLoader(registry);

		final var source = Files.readString(
			Path.of("src/test/model/Peugeot.lm"), StandardCharsets.UTF_8);

		final LmDocument doc = loader.loadModel(source);

		System.out.println("Peugeot.lm diagnostics:");
		doc.diagnostics().forEach(d ->
			System.out.printf("  %s %d:%d %s%n", d.severity(), d.line(), d.column(), d.message()));

		assertEquals(1, doc.roots().size(), "Peugeot.lm should have a single root");
		assertNotNull(doc.source(), "Document source should not be null");

		assertTrue(doc.diagnostics()
					  .stream()
					  .noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
				   "Diagnostics should not contain errors for Peugeot.lm");

		final var objects = loader.loadObjects(source);
		assertEquals(1, objects.size(), "Peugeot.lm should produce a single root object");
		assertInstanceOf(CarCompany.class, objects.getFirst(), "Root object should be a CarCompany instance");
	}
}
