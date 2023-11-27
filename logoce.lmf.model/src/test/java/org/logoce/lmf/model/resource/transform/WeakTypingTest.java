package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Primitive;
import org.logoce.lmf.model.lang.Unit;
import org.logoce.lmf.model.resource.parsing.PTreeReader;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class WeakTypingTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void alias()
	{
		final var textModel = "(MetaModel Test " +
							  "    (Alias Definition \"Group concrete\")" +
							  "    (Definition Oui)" +
							  "    (Alias [1..*]     \"mandatory many\")" +
							  "    (Definition Atts" +
							  "        (-att [1..*] count  #LMCore/units.3)" +
							  "        (+att [1..*] exists #LMCore/units.2)" +
							  "    )" +
							  ") ";

		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelBuilder<>();
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof MetaModel);
		final var model = (MetaModel) root;

		final var alias = model.aliases()
							   .get(0);
		assertEquals("Definition", alias.name());
		final var value = alias.value();
		assertEquals("Group concrete", value);

		final var group0 = model.groups()
								.get(0);
		assertEquals("Oui", group0.name());
		assertTrue(group0.concrete());

		final var group1 = model.groups()
								.get(1);
		assertEquals("Atts", group1.name());
		assertTrue(group1.concrete());
		assertEquals(2,
					 group1.features()
						   .size());

		final var att0 = (Attribute<?, ?>) group1.features()
												 .get(0);
		assertEquals("count", att0.name());
		assertEquals(Primitive.Int, ((Unit<?>) att0.datatype()).primitive());
		assertTrue(att0.many());
		assertTrue(att0.immutable());

		final var att1 = (Attribute<?, ?>) group1.features()
												 .get(1);
		assertEquals("exists", att1.name());
		assertEquals(Primitive.Boolean, ((Unit<?>) att1.datatype()).primitive());
		assertTrue(att1.mandatory());
		assertFalse(att1.immutable());
		assertTrue(att1.many());
	}

	@Test
	public void group()
	{
		final var textModel = """
				(MetaModel Test
				    (Group Container
				        (Generic T Extends #LMCore/groups.0)
				        (-contains cargo [1..1] (reference /groups.2 /groups.0/generics.0))
				    )
				    (Definition Car)
				    (Group CarContainer (includes /groups.0 /groups.1))
				)
				""";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelBuilder<>();
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof MetaModel);
		final var model = (MetaModel) root;

		final var groups = model.groups();
		final var container = groups.get(0);
		final var car = groups.get(1);
		final var carContainer = groups.get(2);

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
	public void simpleUnit()
	{
		final var textModel = "(Unit boolean " +
							  "      \"rgx_match:<(true|false)>\" " +
							  "      false " +
							  "      boolean )";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelBuilder<>();
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Unit<?>);

		final var unit = (Unit<?>) root;
		assertEquals("boolean", unit.name());
		assertEquals(Primitive.Boolean, unit.primitive());
		assertEquals("false", unit.defaultValue());
		assertEquals("rgx_match:<(true|false)>", unit.matcher());
		assertNull(unit.extractor());
	}
}
