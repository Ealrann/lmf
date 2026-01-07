package org.logoce.lmf.core.api.util;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.lang.Named;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModelUtilStreamTreeTest
{
	@Test
	void streamTree_traversesDeepContainments()
	{
		final var metaModelSource = """
			(MetaModel domain=test.model name=StreamTreeMeta
				(Group Named (includes group=#LMCore@Named))
				(Definition Material (includes group=@Named))
				(Definition MaterialsEnv (includes group=#LMCore@LMObject)
					(+contains materials [0..*] @Material))
				(Definition Root (includes group=#LMCore@Model)
					(+contains materials [1..1] @MaterialsEnv)))
			""";
		final var modelSource = """
			(Root domain=test.model name=ModelA metamodels=test.model.StreamTreeMeta
				(MaterialsEnv
					(Material Lava)
					(Material Dirt)))
			""";

		final var registry = loadMetaModelIntoRegistry(metaModelSource);
		final var model = loadModel(registry, modelSource);

		final var objects = ModelUtil.streamTree(model).toList();
		final var groupNames = objects.stream().map(o -> o.lmGroup().name()).toList();
		assertEquals(java.util.List.of("Root", "MaterialsEnv", "Material", "Material"), groupNames);

		final var names = objects.stream()
								 .filter(Named.class::isInstance)
								 .map(Named.class::cast)
								 .map(Named::name)
								 .filter(Objects::nonNull)
								 .sorted()
								 .toList();
		assertEquals(java.util.List.of("Dirt", "Lava", "ModelA"), names);
	}

	private static ModelRegistry loadMetaModelIntoRegistry(final String metaModelSource)
	{
		final var base = ModelRegistry.empty();
		final var metaDoc = new LmLoader(base).loadModel(metaModelSource);
		assertTrue(metaDoc.diagnostics().stream().noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
				   "MetaModel should load without errors");

		final var metaModel = assertInstanceOf(MetaModel.class, metaDoc.model());
		final var builder = new ModelRegistry.Builder(base);
		builder.register(metaModel);
		return builder.build();
	}

	private static Model loadModel(final ModelRegistry registry, final String source)
	{
		final var doc = new LmLoader(registry).loadModel(source);
		assertTrue(doc.diagnostics().stream().noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
				   "Model should load without errors");
		return assertInstanceOf(Model.class, doc.model());
	}
}
