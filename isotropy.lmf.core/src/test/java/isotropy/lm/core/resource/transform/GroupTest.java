package isotropy.lm.core.resource.transform;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.resource.ptree.PTreeReader;
import isotropy.lmf.core.resource.transform.PTreeToJava;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void simpleGroup()
	{
		final var textModel = """
				(Group name=Container)
				(Definition name=Car)
				""";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

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
				(Model Test
				    (Group name=Container parameters=/groups.0/generics.0
				        (Generic name=T boundType=Extends type=#LMCore/groups.0)
				        (-contains cargo [1..1] (reference group=/groups.2 parameters=/groups.0/generics.0))
				    )
				    (Definition name=Car)
				    (Group name=CarContainer (includes group=/groups.0 parameters=/groups.1))
				)
				""";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Model);
		final var model = (Model) root;

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
								 .get(0));
	}

	@Test
	public void multipleRef()
	{
		final var textModel = """
				(Model LMCore
					(Group Feature
					    (Generic UnaryType)
					    (Generic EffectiveType))
					(Group Attribute (includes /groups.0 parameters=/groups.1/generics.0,/groups.1/generics.1)
						(Generic UnaryType) (Generic EffectiveType))
				)
				""";

		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Model);
		final var model = (Model) root;

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
		assertEquals(parameters.get(0), attGeneric0);
		assertEquals(parameters.get(0), attGeneric0);
		assertEquals(parameters.get(1), attGeneric1);
	}
}
