package org.logoce.lmf.core.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.JavaWrapper;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.loader.LmLoader;
import org.logoce.lmf.core.util.ModelRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaWrapperTest
{
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
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		final var model = (MetaModel) roots.get(0);

		final var group = model.groups()
							   .get(0);
		final var attribute = (Attribute<?, ?, ?, ?>) group.features()
														 .get(0);
		final var wrapper = (JavaWrapper<?>) attribute.datatype();

		assertEquals("IModelPackage", wrapper.name());
		assertEquals("org.logoce.lmf.core.api.model.IModelPackage", wrapper.qualifiedClassName());
	}
}
