package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.resource.parsing.PTreeReader;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GenericTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void simpleGeneric()
	{
		final var textModel = "(Generic name=UnaryType boundType=Extends type=#LMCore/groups.0)";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Generic);

		final var generic = (Generic<?>) root;
		assertEquals("UnaryType", generic.name());
		assertEquals(BoundType.Extends, generic.boundType());
		assertEquals(generic.type(), LMCorePackage.MODEL.groups().get(0));
	}

	@Test
	public void genericWithLocalGroup()
	{
		final var textModel = "(MetaModel Test " +
							  "  (Group name=GenericGroup" +
							  "    (Generic name=T boundType=Super type=/groups.1))" +
							  "  (Group name=ICategory))";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

		assertTrue(roots.get(0) instanceof MetaModel);

		final var model = (MetaModel) roots.get(0);

		final var group0 = model.groups().get(0);
		final var group1 = model.groups().get(1);
		final var generic = group0.generics().get(0);

		assertEquals("T", generic.name());
		assertEquals(BoundType.Super, generic.boundType());
		assertEquals(generic.type(), group1);
	}

	@Test
	public void genericHalfUsage()
	{
		final var textModel = """
				(MetaModel Test
				    (Group name=Container
				        (Generic name=T boundType=Extends type=#LMCore/groups.0)
				    )
				    (Definition name=Car)
				    (Group name=CarContainer (includes group=/groups.0 parameters=/groups.1))
				)
				""";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

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

		assertEquals(car, carContainer.includes().get(0).parameters().get(0));
	}

	@Test
	public void genericFullUsage()
	{
		final var textModel = """
				(MetaModel Test
				    (Group name=Container
				        (Generic name=T boundType=Extends type=#LMCore/groups.0)
				        (-contains cargo [1..1] /groups.2 parameters=/groups.0/generics.0)
				    )
				    (Definition name=Car)
				    (Group name=CarContainer (includes group=/groups.0 parameters=/groups.1))
				)
				""";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof MetaModel);
		final var model = (MetaModel) root;

		final var container = model.groups().get(0);
		final var car = model.groups().get(1);
		final var carContainer = model.groups().get(2);
		final var genericOfContainer = container.generics().get(0);
		final var cargoRelation = (Relation<?, ?>) container.features().get(0);

		assertEquals("Container", container.name());
		assertEquals(1, container.generics().size());
		assertEquals(1, container.features().size());
		assertEquals("Car", car.name());
		assertEquals("CarContainer", carContainer.name());

		assertEquals(genericOfContainer, cargoRelation.parameters().get(0));
		assertEquals(car, carContainer.includes().get(0).parameters().get(0));

	}
}
