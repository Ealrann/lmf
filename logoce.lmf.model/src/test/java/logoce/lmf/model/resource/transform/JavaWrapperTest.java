package logoce.lmf.model.resource.transform;

import logoce.lmf.model.lang.Attribute;
import logoce.lmf.model.lang.JavaWrapper;
import logoce.lmf.model.lang.Model;
import logoce.lmf.model.resource.ptree.PTreeReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaWrapperTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void group()
	{
		final var textModel = """
				(Model Test
				    (Group name=Container
				    	(-att name=rawFeature datatype=#LMCore/javaWrappers.0)
				    )
				)
				""";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var model = (Model) roots.get(0);

		final var group = model.groups()
							   .get(0);
		final var attribute = (Attribute<?, ?>) group.features()
													 .get(0);
		final var wrapper = (JavaWrapper<?>) attribute.datatype();

		assertEquals("RawFeature", wrapper.name());
		assertEquals("logoce.lmf.model.api.feature", wrapper.domain());
	}
}
