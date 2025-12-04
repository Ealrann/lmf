package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.loader.LmLoader;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupTest
{
	@Test
	public void simpleGroup()
	{
		final var textModel = """
				(Group name=Container)
				(Definition name=Car)
				""";
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		assertTrue(roots.get(0) instanceof Group);
		assertTrue(roots.get(1) instanceof Group);

		final var group0 = (Group<?>) roots.get(0);
		final var group1 = (Group<?>) roots.get(1);

		assertEquals("Container", group0.name());
		assertEquals("Car", group1.name());
	}

	@Test
	public void group()
	{
		final var textModel = """
				(MetaModel Test
				    (Group name=Container
				        (Generic name=T
				            (extension boundType=Extends type=#LMCore/groups.0))
				        (-contains cargo [1..1] /groups.2 (parameters /groups.0/generics.0))
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

		final var container = model.groups()
								   .get(0);
		final var car = model.groups()
							 .get(1);
		final var carContainer = model.groups()
									  .get(2);
		final var genericOfContainer = container.generics()
												.get(0);
		final var cargoRelation = (Relation<?, ?>) container.features()
															.get(0);

		assertEquals(container,
					 carContainer.includes()
								 .get(0)
								 .group());
		assertEquals(car,
					 carContainer.includes()
								 .get(0)
								 .parameters()
								 .get(0)
								 .type());
	}

	@Test
	public void multipleRef()
	{
		final var textModel = """
				(MetaModel LMCore
					(Group Feature
					    (Generic UnaryType)
					    (Generic EffectiveType))
					(Group Attribute (includes group=/groups.0 (parameters type=/groups.1/generics.0) (parameters type=/groups.1/generics.1))
						(Generic UnaryType) (Generic EffectiveType))
				)
				""";

		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		final var root = roots.get(0);
		assertTrue(root instanceof MetaModel);
		final var model = (MetaModel) root;

		final var featureGroup = model.groups()
									  .get(0);
		final var attGroup = model.groups()
								  .get(1);
		final var attGeneric0 = attGroup.generics()
										.get(0);
		final var attGeneric1 = attGroup.generics()
										.get(1);

		final var include = attGroup.includes()
									.get(0);
		final var parameters = include.parameters();

		assertEquals(include.group(), featureGroup);
		assertEquals(parameters.get(0).type(), attGeneric0);
		assertEquals(parameters.get(0).type(), attGeneric0);
		assertEquals(parameters.get(1).type(), attGeneric1);
	}

	@Test
	public void selfBoundGeneric()
	{
		final var textModel = """
				(MetaModel Test
				    (Group Maintainable
				        (Generic T
				            (extension boundType=Extends type=@Maintainable
				                (parameters ../../../generics.0)))
				        (+refers name=maintainer [0..1] @Maintainable
				            (parameters ../../generics.0)))
				    (Group Maintainer
				        (Generic T
				            (extension boundType=Extends type=@Maintainable
				                (parameters ../../../generics.0)))
				        (+refers name=maintained [0..*] @Maintainable
				            (parameters ../../generics.0)))
				)
				""";

		final var loader = new LmLoader(ModelRegistry.empty());
		loader.loadObjects(textModel);
	}
}
