package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.resource.ptree.PTreeReader;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void simpleEnum()
	{
		final var textModel = "(Enum name=EColor literals=red,green,blue)";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var color = roots.get(0);
		assertTrue(color instanceof Enum<?>);

		final var ecolor = (Enum<?>) color;
		assertEquals("EColor", ecolor.name());
		assertEquals(3 , ecolor.literals().size());
		assertEquals("red", ecolor.literals().get(0));
		assertEquals("green", ecolor.literals().get(1));
		assertEquals("blue", ecolor.literals().get(2));
	}

	@Test
	public void simpleEnumInModel()
	{
		final var textModel = "(Model SimpleEnum (Enum name=EColor literals=red,green,blue))";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var model = roots.get(0);
		assertTrue(model instanceof Model);

		final var color = ((Model) model).enums().get(0);

		final var ecolor = (Enum<?>) color;
		assertEquals("EColor", ecolor.name());
		assertEquals(3 , ecolor.literals().size());
		assertEquals("red", ecolor.literals().get(0));
		assertEquals("green", ecolor.literals().get(1));
		assertEquals("blue", ecolor.literals().get(2));
	}
}
