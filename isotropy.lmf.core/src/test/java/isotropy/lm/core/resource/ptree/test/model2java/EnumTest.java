package isotropy.lm.core.resource.ptree.test.model2java;

import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.resource.ptree.PTreeReader;
import isotropy.lmf.core.resource.transform.PTreeToJava;
import org.junit.jupiter.api.Test;

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
}
