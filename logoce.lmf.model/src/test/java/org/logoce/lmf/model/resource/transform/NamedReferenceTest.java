package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Primitive;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Unit;
import org.logoce.lmf.model.loader.LmLoader;
import org.logoce.lmf.model.util.ModelRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamedReferenceTest
{
	@Test
	public void attributes()
	{
		final var textModel = "(MetaModel Test " +
							  "  (Definition name=Info" +
							  "    (-att [1..1] name=age datatype=#LMCore@int)" +
							  "    (-att [1..1] name=isNice datatype=#LMCore@boolean)" +
							  "    )" +
							  "  (Definition name=Object" +
							  "    (-contains name=info @Info)" +
							  "    )" +
							  ") ";
		final var loader = new LmLoader(ModelRegistry.empty());
		final var roots = loader.loadObjects(textModel);

		final var model = (MetaModel) roots.get(0);

		final var group0 = model.groups().get(0);
		assertEquals("Info", group0.name());
		assertEquals(2, group0.features().size());

		final var att0 = (Attribute<?, ?, ?, ?>) group0.features().get(0);
		assertEquals("age", att0.name());
		assertEquals(Primitive.Int, ((Unit<?>) att0.datatype()).primitive());

		final var att1 = (Attribute<?, ?, ?, ?>) group0.features().get(1);
		assertEquals("isNice", att1.name());
		assertEquals(Primitive.Boolean, ((Unit<?>) att1.datatype()).primitive());

		final var group1 = model.groups().get(1);
		assertEquals("Object", group1.name());
		assertEquals(1, group1.features().size());

		final var rel1 = (Relation<?, ?, ?, ?>) group1.features().get(0);
		assertEquals("info", rel1.name());
		assertEquals(group0, rel1.concept());
	}
}
