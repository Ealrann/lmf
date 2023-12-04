package org.logoce.lmf.model.functional;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.resource.ResourceUtil;
import org.logoce.lmf.model.resource.parsing.PTreeReader;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionalTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void inheritanceTest()
	{
		final var textmodel1 = "(MetaModel Test domain=test1" +
							   "  (Group name=Vehicule" +
							   "    (-att [1..1] name=name datatype=#LMCore@string)" +
							   "    (-att [1..1] name=color datatype=@Color))" +
							   "  (Enum name=Color reg,green,blue)" +
							   ") ";
		final var textmodel2 = "(MetaModel Impl domain=test1 imports=test1.Test" +
							   "  (Definition name=Car" +
							   "    (includes #Test@Vehicule)" +
							   "    (-att [1..1] name=speed datatype=#LMCore@float))" +
							   ") ";

		final var inputStream1 = new ByteArrayInputStream(textmodel1.getBytes());
		final var roots1 = ResourceUtil.loadModel(inputStream1);
		final var model1 = (MetaModel) roots1.get(0);
		final var vehicule = model1.groups().get(0);

		assertEquals("Vehicule", vehicule.name());

		ModelRegistry.Instance.register(model1);

		final var inputStream2 = new ByteArrayInputStream(textmodel2.getBytes());
		final var roots2 = ResourceUtil.loadModel(inputStream2);
		final var model2 = (MetaModel) roots2.get(0);
		final var car = model2.groups().get(0);

		assertEquals("Car", car.name());
		assertEquals(vehicule, car.includes().get(0).group());
	}

}
