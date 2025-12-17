package org.logoce.lmf.core.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.api.model.ModelRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FullMetaModelTest
{
	@Test
	public void loadFullModel() throws IOException
	{
		final var loader = new LmLoader(ModelRegistry.empty());
		try (final var inputStream = Files.newInputStream(Path.of("src/main/model/asset/LMCore.lm")))
		{
			final var document = loader.loadModel(inputStream);
			assertNotNull(document.model(), "LMCore.lm should produce a MetaModel");
			assertFalse(document.diagnostics()
								.stream()
								.anyMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
						"LMCore.lm should be free of error diagnostics");

			final var model = (MetaModel) document.model();
			assertTrue(model.groups().size() > 0);
			assertTrue(model.aliases().size() > 0);
			assertTrue(model.units().size() > 0);
			assertTrue(model.enums().size() > 0);
		}
	}
}
