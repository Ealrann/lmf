package org.logoce.lmf.model.functional;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.resource.ResourceUtil;
import org.logoce.lmf.model.resource.parsing.PTreeReader;

import java.io.ByteArrayInputStream;

public class FunctionalTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void inheritanceTest()
	{
		final var model1 = "(MetaModel Test domain=test1" +
						   "  (Group name=Vehicule" +
						   "    (-att [1..1] name=name datatype=#LMCore@string)" +
						   "    (-att [1..1] name=color datatype=@Color))" +
						   "  (Enum name=Color reg,green,blue)" +
						   ") ";
		final var model2 = "(MetaModel Impl domain=test1 imports=test1.Test" +
						   "  (Definition name=Car" +
						   "    (includes #Test@Vehicule)" +
						   "    (-att [1..1] name=speed datatype=#LMCore@float))" +
						   ") ";

		final var inputStream = new ByteArrayInputStream(model1.getBytes());
		final var roots = ResourceUtil.loadModel(inputStream);

		final var m1 = (MetaModel) roots.get(0);

		final var vehicule = m1.groups().get(0);

	}

}
