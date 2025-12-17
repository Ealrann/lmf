package org.logoce.lmf.core.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.api.model.ModelRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumTest
{
	@Test
	public void simpleEnum()
	{
		final var textModel = "(Enum name=EColor literals=red,green,blue)";
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

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
		final var textModel = "(MetaModel SimpleEnum (Enum name=EColor literals=red,green,blue))";
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		final var model = roots.get(0);
		assertTrue(model instanceof MetaModel);

		final var color = ((MetaModel) model).enums().get(0);

		final var ecolor = (Enum<?>) color;
		assertEquals("EColor", ecolor.name());
		assertEquals(3 , ecolor.literals().size());
		assertEquals("red", ecolor.literals().get(0));
		assertEquals("green", ecolor.literals().get(1));
		assertEquals("blue", ecolor.literals().get(2));
	}
}
