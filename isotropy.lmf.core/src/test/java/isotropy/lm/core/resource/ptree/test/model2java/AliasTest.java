package isotropy.lm.core.resource.ptree.test.model2java;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.resource.ptree.PTreeReader;
import isotropy.lmf.core.resource.transform.PTreeToJava;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class AliasTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void simpleAlias()
	{
		final var textModel = "(Alias name=Definition words=Group,concrete) ";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Alias);

		final var alias = (Alias) root;
		assertEquals("Definition", alias.name());
		assertEquals(2,
					 alias.words()
						  .size());
		assertEquals("Group",
					 alias.words()
						  .get(0));
		assertEquals("concrete",
					 alias.words()
						  .get(1));
	}

	@Test
	public void alias()
	{
		final var textModel = "(Model Test " +
							  "    (Alias name=Definition words=Group,concrete)" +
							  "    (Definition name=Oui)" +
							  "    (Alias name=[1..*]     mandatory,many)" +
							  "    (Definition name=Atts" +
							  "        (-att [1..*] name=count dataType=#LMCore/units.3)" +
							  "        (+att [1..*] name=exists dataType=#LMCore/units.2)" +
							  "    )" +
							  ") ";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Model);
		final var model = (Model) root;

		final var alias = model.aliases()
							   .get(0);
		assertEquals("Definition", alias.name());
		final var words = alias.words();
		assertEquals(2, words.size());
		assertEquals("Group", words.get(0));
		assertEquals("concrete", words.get(1));

		final var group0 = model.groups()
								.get(0);
		assertEquals("Oui", group0.name());
		assertTrue(group0.concrete());

		final var group1 = model.groups()
								.get(1);
		assertEquals("Oui", group1.name());
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
												 .get(0);
		assertEquals("count", att1.name());
		assertEquals(Primitive.Boolean, ((Unit<?>) att1.datatype()).primitive());
		assertTrue(att1.many());
		assertFalse(att1.immutable());
	}
}
