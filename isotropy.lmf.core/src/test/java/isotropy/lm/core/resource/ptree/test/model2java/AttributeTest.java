package isotropy.lm.core.resource.ptree.test.model2java;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.lang.Primitive;
import isotropy.lmf.core.lang.Unit;
import isotropy.lmf.core.resource.ptree.PTreeReader;
import isotropy.lmf.core.resource.transform.PTreeToJava;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class AttributeTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void attributes()
	{
		final var textModel = "(Model Test " +
							  "  (Definition name=Atts" +
							  "    (Attribute mandatory immutable many name=count datatype=#LMCore/units.3)" +
							  "    (Attribute mandatory=false immutable=false many=false name=exists " +
							  "        datatype=#LMCore/units.2)" +
							  "    )" +
							  ") ";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Model);
		final var model = (Model) root;


		final var group0 = model.groups()
								.get(0);
		assertEquals("Atts", group0.name());
		assertTrue(group0.concrete());
		assertEquals(2,
					 group0.features()
						   .size());

		final var att0 = (Attribute<?, ?>) group0.features()
												 .get(0);
		assertEquals("count", att0.name());
		assertEquals(Primitive.Int, ((Unit<?>) att0.datatype()).primitive());
		assertTrue(att0.many());
		assertTrue(att0.immutable());
		assertTrue(att0.mandatory());

		final var att1 = (Attribute<?, ?>) group0.features()
												 .get(1);
		assertEquals("exists", att1.name());
		assertEquals(Primitive.Boolean, ((Unit<?>) att1.datatype()).primitive());
		assertFalse(att1.immutable());
		assertFalse(att1.mandatory());
	}
}
