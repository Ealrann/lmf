package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.resource.parsing.PTreeReader;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FullMetaModelTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void loadFullModel() throws IOException
	{
		final var inputStream = Files.newInputStream(Path.of("src/main/model/asset/LMCore.lm"));
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof MetaModel);

		final var model = (MetaModel) root;
		assertTrue(model.groups().size() > 0);
		assertTrue(model.aliases().size() > 0);
		assertTrue(model.units().size() > 0);
		assertTrue(model.enums().size() > 0);
	}
}
