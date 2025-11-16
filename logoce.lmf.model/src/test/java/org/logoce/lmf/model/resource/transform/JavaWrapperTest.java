package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.resource.parsing.PTreeReader;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaWrapperTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void group()
	{
		final var textModel = """
				(MetaModel Test
				    (Group name=Container
				    	(-att name=rawFeature datatype=#LMCore/javaWrappers.0)
				    )
				)
				""";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelLinker<>(ModelRegistry.empty());
		final var roots = ptreeToJava.build(ptree);

		final var model = (MetaModel) roots.get(0);

		final var group = model.groups()
							   .get(0);
		final var attribute = (Attribute<?, ?>) group.features()
													 .get(0);
		final var wrapper = (JavaWrapper<?>) attribute.datatype();

		assertEquals("RawFeature", wrapper.name());
		assertEquals("org.logoce.lmf.model.api.feature", wrapper.domain());
	}
}
