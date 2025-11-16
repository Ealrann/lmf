package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.resource.parsing.PTreeReader;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void singleElement()
	{
		final var textModel = "(MetaModel domain=test.model name=World)";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof MetaModel);

		final var model = (MetaModel) root;
		assertEquals("World", model.name());
		assertEquals("test.model", model.domain());
	}

	@Test
	public void twoClasses()
	{
		final var textModel = "(Group Car) (Group Chair)";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

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
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

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
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

		final var color = roots.get(0);
		assertTrue(color instanceof Enum<?>);
		assertEquals("EColor", ((Enum<?>) color).name());

		final var size = roots.get(1);
		assertTrue(size instanceof Enum<?>);
		assertEquals("ESize", ((Enum<?>) size).name());
	}
}
