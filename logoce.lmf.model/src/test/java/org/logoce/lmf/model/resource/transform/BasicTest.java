package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.resource.ptree.PTreeReader;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void singleElement()
	{
		final var textModel = "(Model test.model:World)";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Model);

		final var model = (Model) root;
		assertEquals("test.model:World", model.name());
	}

	@Test
	public void twoClasses()
	{
		final var textModel = "(Group Car) (Group Chair)";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

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
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var car = roots.get(0);
		assertTrue(car instanceof Group);
		assertTrue(((Group<?>) car).concrete());
		assertEquals("Car", ((Group<?>) car).name());
	}

	@Test
	public void twoEmptyEnums()
	{
		final var textModel = "(Enum EColor) (Enum ESize)";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var color = roots.get(0);
		assertTrue(color instanceof Enum<?>);
		assertEquals("EColor", ((Enum<?>) color).name());

		final var size = roots.get(1);
		assertTrue(size instanceof Enum<?>);
		assertEquals("ESize", ((Enum<?>) size).name());
	}
}
