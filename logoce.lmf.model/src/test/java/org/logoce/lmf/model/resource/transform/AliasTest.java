package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.resource.parsing.PTreeReader;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class AliasTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void simpleAlias()
	{
		final var textModel = "(Alias name=Definition value=\"Group concrete\") ";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Alias);

		final var alias = (Alias) root;
		assertEquals("Definition", alias.name());
		assertEquals("Group concrete", alias.value());
	}

	@Test
	public void simpleAssignAlias()
	{
		final var textModel = "(Alias name=Definition \"Group concrete=false contains=true\")";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Alias);

		final var alias = (Alias) root;
		assertEquals("Definition", alias.name());
		assertEquals("Group concrete=false contains=true", alias.value());
	}

	@Test
	public void simpleAliasResolution()
	{
		final var textModel = "(-att [1..*] name=count datatype=#LMCore/units.3)";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Attribute<?, ?>);
		final var att0 = (Attribute<?, ?>) root;

		assertEquals("count", att0.name());
		assertEquals(Primitive.Int, ((Unit<?>) att0.datatype()).primitive());
		assertTrue(att0.many());
		assertTrue(att0.immutable());
	}

	@Test
	public void alias()
	{
		final var textModel = "(MetaModel Test " +
							  "    (Alias name=Definition value=\"Group concrete\")" +
							  "    (Definition name=Oui)" +
							  "    (Alias name=[1..*]     value=\"mandatory many\")" +
							  "    (Definition name=Atts" +
							  "        (-att [1..*] name=count datatype=#LMCore/units.3)" +
							  "        (+att [1..*] name=exists datatype=#LMCore/units.2)" +
							  "    )" +
							  ") ";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof MetaModel);
		final var model = (MetaModel) root;

		final var alias = model.aliases().get(0);
		assertEquals("Definition", alias.name());

		final var group0 = model.groups().get(0);
		assertEquals("Oui", group0.name());
		assertTrue(group0.concrete());

		final var group1 = model.groups().get(1);
		assertEquals("Atts", group1.name());
		assertTrue(group1.concrete());
		assertEquals(2, group1.features().size());

		final var att0 = (Attribute<?, ?>) group1.features().get(0);
		assertEquals("count", att0.name());
		assertEquals(Primitive.Int, ((Unit<?>) att0.datatype()).primitive());
		assertTrue(att0.many());
		assertTrue(att0.immutable());

		final var att1 = (Attribute<?, ?>) group1.features().get(1);
		assertEquals("exists", att1.name());
		assertEquals(Primitive.Boolean, ((Unit<?>) att1.datatype()).primitive());
		assertTrue(att1.mandatory());
		assertFalse(att1.immutable());
		assertTrue(att1.many());
	}
}
