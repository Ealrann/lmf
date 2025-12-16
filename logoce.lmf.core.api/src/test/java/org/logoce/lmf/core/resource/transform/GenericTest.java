package org.logoce.lmf.core.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.lang.BoundType;
import org.logoce.lmf.core.lang.Generic;
import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.api.loader.LmLoader;
import org.logoce.lmf.core.api.model.ModelRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GenericTest
{
	@Test
	public void simpleGeneric()
	{
		final var textModel = "(Generic name=UnaryType (GenericExtension boundType=Extends type=#LMCore/groups.0))";
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		final var root = roots.get(0);
		assertTrue(root instanceof Generic);

		final var generic = (Generic<?>) root;
		assertEquals("UnaryType", generic.name());
		assertNotNull(generic.extension());
		assertEquals(BoundType.Extends, generic.extension().boundType());
		assertEquals(generic.extension().type(), LMCoreModelPackage.MODEL.groups().get(0));
	}

	@Test
	public void genericWithLocalGroup()
	{
		final var textModel = "(MetaModel Test " +
							  "  (Group name=GenericGroup" +
							  "    (Generic name=T (GenericExtension boundType=Super type=/groups.1)))" +
							  "  (Group name=ICategory))";
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		assertTrue(roots.get(0) instanceof MetaModel);

		final var model = (MetaModel) roots.get(0);

		final var group0 = model.groups().get(0);
		final var group1 = model.groups().get(1);
		final var generic = group0.generics().get(0);

		assertEquals("T", generic.name());
		assertNotNull(generic.extension());
		assertEquals(BoundType.Super, generic.extension().boundType());
		assertEquals(generic.extension().type(), group1);
	}

	@Test
	public void genericHalfUsage()
	{
		final var textModel = """
				(MetaModel Test
				    (Group name=Container
				        (Generic name=T
				            (GenericExtension boundType=Extends type=#LMCore/groups.0))
				    )
				    (Definition name=Car)
				    (Group name=CarContainer (includes group=/groups.0 (parameters type=/groups.1)))
				)
				""";
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		final var root = roots.get(0);
		assertTrue(root instanceof MetaModel);
		final var model = (MetaModel) root;

		final var container = model.groups().get(0);
		final var car = model.groups().get(1);
		final var carContainer = model.groups().get(2);

		assertEquals("Container", container.name());
		assertEquals(1, container.generics().size());
		assertEquals(0, container.features().size());
		assertEquals("Car", car.name());
		assertEquals("CarContainer", carContainer.name());

		final var generic = container.generics().get(0);
		assertNotNull(generic.extension());
		assertEquals(BoundType.Extends, generic.extension().boundType());
		assertEquals(generic.extension().type(), LMCoreModelPackage.MODEL.groups().get(0));

		assertEquals(car, carContainer.includes().get(0).parameters().get(0).type());
	}

	@Test
	public void genericFullUsage()
	{
		final var textModel = """
				(MetaModel Test
				    (Group name=Container
				        (Generic name=T
				            (GenericExtension boundType=Extends type=#LMCore/groups.0))
				        (-contains cargo [1..1] /groups.2 (parameters type=/groups.0/generics.0))
				    )
				    (Definition name=Car)
				    (Group name=CarContainer (includes group=/groups.0 (parameters type=/groups.1)))
				)
				""";
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		final var root = roots.get(0);
		assertTrue(root instanceof MetaModel);
		final var model = (MetaModel) root;

		final var container = model.groups().get(0);
		final var car = model.groups().get(1);
		final var carContainer = model.groups().get(2);
		final var genericOfContainer = container.generics().get(0);
		final var cargoRelation = (Relation<?, ?, ?, ?>) container.features().get(0);

		assertEquals("Container", container.name());
		assertEquals(1, container.generics().size());
		assertEquals(1, container.features().size());
		assertEquals("Car", car.name());
		assertEquals("CarContainer", carContainer.name());

		assertEquals(genericOfContainer, cargoRelation.parameters().get(0).type());
		assertEquals(car, carContainer.includes().get(0).parameters().get(0).type());

		assertNotNull(genericOfContainer.extension());
		assertEquals(BoundType.Extends, genericOfContainer.extension().boundType());
		assertEquals(genericOfContainer.extension().type(), LMCoreModelPackage.MODEL.groups().get(0));
	}
}
