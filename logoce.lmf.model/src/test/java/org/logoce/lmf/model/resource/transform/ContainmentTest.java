package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.resource.parsing.PTreeReader;

import java.io.ByteArrayInputStream;

public class ContainmentTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void simpleEnumInModel()
	{
		final var textModel = "(MetaModel name=Containment" +
							  "(Enum name=EColor literals=red,green,blue)" +
							  "(Group name=Toto))";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelBuilder<>();
		final var roots = ptreeToJava.build(ptree);

		final var model = (MetaModel) roots.get(0);
		final var color = model.enums()
							   .get(0);
		final var group = model.groups()
							   .get(0);

		Assertions.assertEquals(model, color.lmContainer());
		Assertions.assertEquals(LMCoreDefinition.Features.META_MODEL.ENUMS, color.lmContainingFeature());

		Assertions.assertEquals(model, group.lmContainer());
		Assertions.assertEquals(LMCoreDefinition.Features.META_MODEL.GROUPS, group.lmContainingFeature());
	}
}
