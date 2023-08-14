package isotropy.lm.core.resource.ptree.test.model2java;

import isotropy.lmf.core.lang.BoundType;
import isotropy.lmf.core.lang.Generic;
import isotropy.lmf.core.lang.LMCorePackage;
import isotropy.lmf.core.resource.ptree.PTreeReader;
import isotropy.lmf.core.resource.transform.PTreeToJava;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GenericTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void simpleEnum()
	{
		final var textModel = "(Generic name=UnaryType boundType=Extends type=#LMCore/groups.0)";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Generic);

		final var generic = (Generic) root;
		assertEquals("UnaryType", generic.name());
		assertEquals(BoundType.Extends, generic.boundType());
		assertEquals(generic.type(), LMCorePackage.MODEL.groups().get(0));
	}
}
