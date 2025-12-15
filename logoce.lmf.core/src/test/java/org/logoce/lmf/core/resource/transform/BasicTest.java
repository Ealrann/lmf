package org.logoce.lmf.core.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.loader.LmLoader;
import org.logoce.lmf.core.api.model.ModelRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicTest
{
	@Test
	public void singleElement()
	{
		final var textModel = "(MetaModel domain=test.model name=World)";
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		final var model = (MetaModel) roots.get(0);
		assertEquals("World", model.name());
		assertEquals("test.model", model.domain());
	}

	@Test
	public void twoClasses()
	{
		final var textModel = "(Group Car) (Group Chair)";
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		final var car = roots.get(0);
		assertTrue(car instanceof Group);
		assertEquals("Car", ((Group<?>) car).name());

		final var chair = roots.get(1);
		assertTrue(chair instanceof Group);
		assertEquals("Chair", ((Group<?>) chair).name());
	}

	@Test
	public void groupWithAttributes()
	{
		final var textModel = "(Group Car concrete)";
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		final var car = roots.get(0);
		assertTrue(car instanceof Group);
		assertTrue(((Group<?>) car).concrete());
		assertEquals("Car", ((Group<?>) car).name());
	}

	@Test
	public void twoEmptyEnums()
	{
		final var textModel = "(Enum EColor) (Enum ESize)";
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		final var color = roots.get(0);
		assertTrue(color instanceof Enum<?>);
		assertEquals("EColor", ((Enum<?>) color).name());

		final var size = roots.get(1);
		assertTrue(size instanceof Enum<?>);
		assertEquals("ESize", ((Enum<?>) size).name());
	}
}
