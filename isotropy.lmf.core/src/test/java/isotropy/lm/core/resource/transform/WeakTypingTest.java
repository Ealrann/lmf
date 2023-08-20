package isotropy.lm.core.resource.transform;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.resource.ptree.PTreeReader;
import isotropy.lmf.core.resource.transform.PTreeToJava;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class WeakTypingTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void alias()
	{
		final var textModel = "(Model Test " +
							  "    (Alias Definition Group,concrete)" +
							  "    (Definition Oui)" +
							  "    (Alias [1..*]     mandatory,many)" +
							  "    (Definition Atts" +
							  "        (-att [1..*] count  #LMCore/units.3)" +
							  "        (+att [1..*] exists #LMCore/units.2)" +
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
				(Model Test
				    (Group Container /groups.0/generics.0
				        (Generic T Extends #LMCore/groups.0)
				        (-contains cargo [1..1] (reference /groups.2 /groups.0/generics.0))
				    )
				    (Definition Car)
				    (Group CarContainer (includes /groups.0 /groups.1))
				)
				""";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Model);
		final var model = (Model) root;

		final var container = model.groups().get(0);
		final var car = model.groups().get(1);
		final var carContainer = model.groups().get(2);

		assertEquals(container, carContainer.includes().get(0).group());
		assertEquals(car, carContainer.includes().get(0).parameters().get(0));
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
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

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
