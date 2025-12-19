package org.logoce.lmf.core.loader;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.api.util.ModelUtil;
import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;

import static org.junit.jupiter.api.Assertions.*;

public final class M1InheritedContainmentWrapperTest
{
	@Test
	void m1_canUseContainmentWrapperForInheritedContainment() throws Exception
	{
		final var metaLoader = LmLoader.withEmptyRegistry();
		final var metaSource = """
			(MetaModel domain=test.model name=InheritedContainment
				(Definition ResourcePkg)
				(Group Container
					(+contains resourcePkg [0..1] @ResourcePkg))
				(Definition Subpass
					(includes group=@Container)
					(includes group=#LMCore@Model))
			)
			""";
		final var metaDoc = metaLoader.loadModel(metaSource);
		assertTrue(metaDoc.diagnostics()
						  .stream()
						  .noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
				   "Meta-model diagnostics should not contain errors");
		assertInstanceOf(MetaModel.class, metaDoc.model(), "Meta-model root should be a MetaModel");

		final var metaModel = (MetaModel) metaDoc.model();

		final var registryBuilder = new ModelRegistry.Builder();
		registryBuilder.register(LMCoreModelPackage.MODEL);
		registryBuilder.register(metaModel);
		final var registry = registryBuilder.build();
		final var loader = new LmLoader(registry);

		final var m1Source = """
			(Subpass domain=test.model name=Instance metamodels=test.model.InheritedContainment
				(resourcePkg))
			""";
		final var objects = loader.loadObjects(m1Source);
		assertEquals(1, objects.size(), "M1 source should produce a single root object");

		final var root = (LMObject) objects.getFirst();
		assertEquals("Subpass", root.lmGroup().name(), "Root object should be a Subpass instance");

		final var subpassGroup = metaModel.groups()
										 .stream()
										 .filter(g -> "Subpass".equals(g.name()))
										 .findFirst()
										 .orElseThrow();
		@SuppressWarnings("unchecked")
		final var resourcePkgRelation = (Relation<LMObject, ?, ?, ?>) ModelUtil.streamAllFeatures(subpassGroup)
																			   .filter(Relation.class::isInstance)
																			   .map(Relation.class::cast)
																			   .filter(f -> "resourcePkg".equals(f.name()))
																			   .findFirst()
																			   .orElseThrow();

		final var resourcePkg = (LMObject) root.get(resourcePkgRelation);
		assertNotNull(resourcePkg, "resourcePkg wrapper should create a contained object");
		assertEquals("ResourcePkg", resourcePkg.lmGroup().name(), "Contained object should be ResourcePkg");
		assertSame(root, resourcePkg.lmContainer(), "Contained object should be owned by root");
		assertSame(resourcePkgRelation, resourcePkg.lmContainingFeature(), "Containing feature should be resourcePkg");
	}
}
