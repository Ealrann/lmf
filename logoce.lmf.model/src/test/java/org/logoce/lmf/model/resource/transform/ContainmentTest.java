package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.loader.LmLoader;
import org.logoce.lmf.model.util.ModelRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContainmentTest
{
	@Test
	public void simpleEnumInModel()
	{
		final var textModel = "(MetaModel name=Containment" +
							  "(Enum name=EColor literals=red,green,blue)" +
							  "(Group name=Toto))";
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		final var model = (MetaModel) roots.get(0);
		final var color = model.enums()
							   .get(0);
		final var group = model.groups()
							   .get(0);

		assertEquals(model, color.lmContainer());
		assertEquals(LMCoreDefinition.Features.META_MODEL.ENUMS, color.lmContainingFeature());

		assertEquals(model, group.lmContainer());
		assertEquals(LMCoreDefinition.Features.META_MODEL.GROUPS, group.lmContainingFeature());
	}
}
