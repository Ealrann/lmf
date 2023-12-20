package org.logoce.lmf.model.functional;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.resource.ResourceUtil;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionalTest
{
	public static final String TEXTMODEL_1 = "(MetaModel name=Test domain=test1" +
											 "  (Group name=Vehicule" +
											 "    (-att [1..1] name=name datatype=#LMCore@string)" +
											 "    (-att [1..1] name=color datatype=@Color))" +
											 "  (Enum name=Color reg,green,blue)" +
											 ") ";
	public static final String TEXTMODEL_2 = "(MetaModel name=Impl1 domain=test1 imports=test1.Test" +
											 "  (Definition name=Car" +
											 "    (includes #Test@Vehicule)" +
											 "    (-att [1..1] name=speed datatype=#LMCore@float))" +
											 ") ";
	public static final String TEXTMODEL_3 = "(MetaModel name=Impl2 domain=test1 imports=test1.Test" +
											 "  (Definition name=Truck" +
											 "    (includes #Test@Vehicule)" +
											 "    (-att [1..1] name=speed datatype=#LMCore@float))" +
											 ") ";
	public static final String TEXTMODEL_4 = "(MetaModel name=Impl1a domain=test1 imports=test1.Impl1" +
											 "  (Definition name=ElectricCar" +
											 "    (includes #Impl1@Car)" +
											 "    (-att [1..1] name=battery datatype=#LMCore@int))" +
											 ") ";

	@Test
	public void manualImportTest()
	{
		final var inputStream1 = new ByteArrayInputStream(TEXTMODEL_1.getBytes());
		final var inputStream2 = new ByteArrayInputStream(TEXTMODEL_2.getBytes());
		final var inputStream3 = new ByteArrayInputStream(TEXTMODEL_3.getBytes());
		final var inputStream4 = new ByteArrayInputStream(TEXTMODEL_4.getBytes());

		final var model1 = (MetaModel) ResourceUtil.loadModel(inputStream1);
		ModelRegistry.Instance.register(model1);
		final var model2 = (MetaModel) ResourceUtil.loadModel(inputStream2);
		ModelRegistry.Instance.register(model2);
		final var model3 = (MetaModel) ResourceUtil.loadModel(inputStream3);
		final var model4 = (MetaModel) ResourceUtil.loadModel(inputStream4);

		check(model1, model2, model3, model4);
	}

	@Test
	public void autoImportTest()
	{
		final var inputStream1 = new ByteArrayInputStream(TEXTMODEL_1.getBytes());
		final var inputStream2 = new ByteArrayInputStream(TEXTMODEL_2.getBytes());
		final var inputStream3 = new ByteArrayInputStream(TEXTMODEL_3.getBytes());
		final var inputStream4 = new ByteArrayInputStream(TEXTMODEL_4.getBytes());
		final var models = ResourceUtil.loadModels(List.of(inputStream1, inputStream2, inputStream3, inputStream4));
		final var model1 = (MetaModel) models.get(0);
		final var model2 = (MetaModel) models.get(1);
		final var model3 = (MetaModel) models.get(2);
		final var model4 = (MetaModel) models.get(3);

		check(model1, model2, model3, model4);
	}

	@Test
	public void autoImportDisorderedTest()
	{
		final var inputStream1 = new ByteArrayInputStream(TEXTMODEL_1.getBytes());
		final var inputStream2 = new ByteArrayInputStream(TEXTMODEL_2.getBytes());
		final var inputStream3 = new ByteArrayInputStream(TEXTMODEL_3.getBytes());
		final var inputStream4 = new ByteArrayInputStream(TEXTMODEL_4.getBytes());
		final var models = ResourceUtil.loadModels(List.of(inputStream4, inputStream2, inputStream3, inputStream1));
		final var model1 = (MetaModel) models.get(3);
		final var model2 = (MetaModel) models.get(1);
		final var model3 = (MetaModel) models.get(2);
		final var model4 = (MetaModel) models.get(0);

		check(model1, model2, model3, model4);
	}

	private static void check(final MetaModel model1,
							  final MetaModel model2,
							  final MetaModel model3,
							  final MetaModel model4)
	{
		final var vehicule = model1.groups().get(0);
		assertEquals("Vehicule", vehicule.name());

		final var car = model2.groups().get(0);
		assertEquals("Car", car.name());
		assertEquals(vehicule, car.includes().get(0).group());

		final var truck = model3.groups().get(0);
		assertEquals("Truck", truck.name());
		assertEquals(vehicule, truck.includes().get(0).group());

		final var ecar = model4.groups().get(0);
		assertEquals("ElectricCar", ecar.name());
		assertEquals(car, ecar.includes().get(0).group());
	}
}
