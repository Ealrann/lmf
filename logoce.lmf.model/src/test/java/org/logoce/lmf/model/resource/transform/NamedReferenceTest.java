package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.resource.parsing.PTreeReader;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NamedReferenceTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void attributes()
	{
		final var textModel = "(Model Test " +
							  "  (Definition name=Info" +
							  "    (-att [1..1] name=age datatype=#LMCore@int)" +
							  "    (-att [1..1] name=isNice datatype=#LMCore@boolean)" +
							  "    )" +
							  "  (Definition name=Object" +
							  "    (-contains name=info (reference group=@Info))" +
							  "    )" +
							  ") ";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelBuilder<>();
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Model);
		final var model = (Model) root;

		final var group0 = model.groups().get(0);
		assertEquals("Info", group0.name());
		assertEquals(2, group0.features().size());

		final var att0 = (Attribute<?, ?>) group0.features().get(0);
		assertEquals("age", att0.name());
		assertEquals(Primitive.Int, ((Unit<?>) att0.datatype()).primitive());

		final var att1 = (Attribute<?, ?>) group0.features().get(1);
		assertEquals("isNice", att1.name());
		assertEquals(Primitive.Boolean, ((Unit<?>) att1.datatype()).primitive());

		final var group1 = model.groups().get(1);
		assertEquals("Object", group1.name());
		assertEquals(1, group1.features().size());

		final var rel1 = (Relation<?, ?>) group1.features().get(0);
		assertEquals("info", rel1.name());
		assertEquals(group0, rel1.reference().group());
	}
}
