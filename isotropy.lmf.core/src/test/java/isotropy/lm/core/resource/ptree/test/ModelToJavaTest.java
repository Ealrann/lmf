package isotropy.lm.core.resource.ptree.test;

import isotropy.lmf.core.lang.LMCorePackage;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.resource.ptree.PTreeReader;
import isotropy.lmf.core.resource.transform.PTreeToJava;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelToJavaTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void testPTreeBuilder_singleElement()
	{
		final var core = LMCorePackage.Instance;


		final var textModel = "(Model test.model:World)";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());

		final var ptree = treeBuilder.read(inputStream);

		final var ptreeToJava = new PTreeToJava();
		final var root = ptreeToJava.transform(ptree);

		assertTrue(root instanceof Model);

		final var model = (Model) root;
		assertEquals("test.model:World", model.name());
	}
}
