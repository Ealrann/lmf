package org.logoce.lmf.core.loader;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.lang.Named;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.feature.EnumResolver;
import org.logoce.lmf.core.loader.api.loader.model.LmDocument;
import org.logoce.lmf.core.api.model.DynamicModelPackage;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.api.util.ModelUtil;
import test.model.carcompany.CarCompany;
import test.model.carcompany.CarCompanyModelPackage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public final class M1LoaderTest
{
	@Test
	void loadPeugeotM1Model_usesCarCompanyAsMetamodel() throws IOException
	{
		final var registryBuilder = new ModelRegistry.Builder();
		registryBuilder.register(LMCoreModelPackage.MODEL);
		registryBuilder.register(CarCompanyModelPackage.MODEL);
		final var registry = registryBuilder.build();

		final var loader = new LmLoader(registry);

		final var source = Files.readString(Path.of("src/test/model/Peugeot.lm"), StandardCharsets.UTF_8);

		final LmDocument doc = loader.loadModel(source);

		assertEquals(1, doc.roots().size(), "Peugeot.lm should have a single root");
		assertNotNull(doc.source(), "Document source should not be null");

		assertTrue(doc.diagnostics()
					  .stream()
					  .noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
				   "Diagnostics should not contain errors for Peugeot.lm");

		final var objects = loader.loadObjects(source);
		assertEquals(1, objects.size(), "Peugeot.lm should produce a single root object");
		assertInstanceOf(CarCompany.class, objects.getFirst(), "Root object should be a CarCompany instance");
	}

	@Test
	void loadPeugeotM1Model_worksWithDynamicallyLoadedMetaModelPackage() throws IOException
	{
		final var metaLoader = LmLoader.withEmptyRegistry();
		final var metaSource = Files.readString(Path.of("src/test/model/CarCompany.lm"), StandardCharsets.UTF_8);
		final var metaDoc = metaLoader.loadModel(metaSource);

		assertTrue(metaDoc.diagnostics()
						  .stream()
						  .noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
				   "CarCompany meta-model diagnostics should not contain errors");
		assertInstanceOf(MetaModel.class, metaDoc.model(), "CarCompany.lm should load as a MetaModel");

		final var carCompanyMetaModel = (MetaModel) metaDoc.model();
		assertNotNull(carCompanyMetaModel.lmPackage(), "Dynamically loaded meta-model should have a package");
		assertInstanceOf(DynamicModelPackage.class,
						 carCompanyMetaModel.lmPackage(),
						 "Dynamically loaded meta-model should be assigned a DynamicModelPackage");

		@SuppressWarnings("unchecked")
		final var brandEnum = (Enum<Object>) carCompanyMetaModel.enums()
															   .stream()
															   .filter(e -> "Brand".equals(e.name()))
															   .findFirst()
															   .orElseThrow();
		assertTrue(brandEnum.literals().contains("Peugeot"),
				   "CarCompany.Brand literals should contain 'Peugeot' but was: " + brandEnum.literals());
		assertSame(carCompanyMetaModel,
				   brandEnum.lmContainer(),
				   "Brand enum should be contained by the loaded CarCompany MetaModel");
		assertEquals("Peugeot",
					 ((DynamicModelPackage) carCompanyMetaModel.lmPackage()).resolveEnumLiteral(brandEnum, "Peugeot")
																		  .orElseThrow()
																		  .toString(),
					 "DynamicModelPackage should resolve enum literal 'Peugeot'");

		@SuppressWarnings("unchecked")
		final Group<LMObject> metaCarGroup = (Group<LMObject>) carCompanyMetaModel.groups()
																				 .stream()
																				 .filter(g -> "Car".equals(g.name()))
																				 .findFirst()
																				 .orElseThrow();

		@SuppressWarnings("unchecked")
		final Attribute<Object, Object, ?, ?> metaBrandAttribute =
			(Attribute<Object, Object, ?, ?>) metaCarGroup.features()
														 .stream()
														 .filter(Attribute.class::isInstance)
														 .filter(f -> "brand".equals(f.name()))
														 .findFirst()
														 .orElseThrow();

		assertSame(brandEnum,
				   metaBrandAttribute.datatype(),
				   "Car.brand datatype should be the Brand enum from the meta-model");

		final var brandResolver = new EnumResolver<>(metaBrandAttribute);
		assertTrue(brandResolver.resolve(java.util.List.of("Peugeot")).isPresent(),
				   "EnumResolver should resolve 'Peugeot' for Car.brand");

		final var registryBuilder = new ModelRegistry.Builder();
		registryBuilder.register(LMCoreModelPackage.MODEL);
		registryBuilder.register(carCompanyMetaModel);
		final var registry = registryBuilder.build();

		final var loader = new LmLoader(registry);
		final var source = Files.readString(Path.of("src/test/model/Peugeot.lm"), StandardCharsets.UTF_8);

		final var objects = loader.loadObjects(source);
		assertEquals(1, objects.size(), "Peugeot.lm should produce a single root object");

		final var root = (LMObject) objects.getFirst();
		assertEquals("CarCompany", root.lmGroup().name(), "Root object should be a CarCompany instance");
		assertInstanceOf(Model.class, root, "Root object should implement Model");
		assertInstanceOf(Named.class, root, "Root object should implement Named");

		final var model = (Model) root;
		final var named = (Named) root;

		assertEquals("test.model", model.domain(), "Model.domain should be resolved via inherited features");
		assertEquals("PeugeotCompany", named.name(), "Named.name should be resolved via inherited features");
		assertEquals(java.util.List.of("test.model.CarCompany"),
					 model.metamodels(),
					 "Model.metamodels should contain the meta-model name from the header");

		final var carCompanyGroup = findGroup(carCompanyMetaModel, "CarCompany");
		final var ceoRelation = findRelation(carCompanyGroup, "ceo");
		final var parcsRelation = findRelation(carCompanyGroup, "parcs");

		final var ceo = (LMObject) root.get(ceoRelation);
		assertNotNull(ceo, "CarCompany.ceo should be present");
		assertEquals("Person", ceo.lmGroup().name(), "CarCompany.ceo should be a Person");
		assertSame(root, ceo.lmContainer(), "Person should be contained by CarCompany");
		assertSame(ceoRelation, ceo.lmContainingFeature(), "Person containing feature should be 'ceo'");

		@SuppressWarnings("unchecked")
		final var parcs = (java.util.List<LMObject>) root.get(parcsRelation);
		assertEquals(1, parcs.size(), "CarCompany.parcs should contain one CarParc");

		final var parc = parcs.getFirst();
		assertEquals("CarParc", parc.lmGroup().name(), "CarCompany.parcs child should be a CarParc");
		assertSame(root, parc.lmContainer(), "CarParc should be contained by CarCompany");
		assertSame(parcsRelation, parc.lmContainingFeature(), "CarParc containing feature should be 'parcs'");

		final var carParcGroup = findGroup(carCompanyMetaModel, "CarParc");
		final var carsRelation = findRelation(carParcGroup, "cars");
		@SuppressWarnings("unchecked")
		final var cars = (java.util.List<LMObject>) parc.get(carsRelation);
		assertEquals(1, cars.size(), "CarParc.cars should contain one Car");

		final var car = cars.getFirst();
		assertEquals("Car", car.lmGroup().name(), "CarParc.cars child should be a Car");
		assertSame(parc, car.lmContainer(), "Car should be contained by CarParc");
		assertSame(carsRelation, car.lmContainingFeature(), "Car containing feature should be 'cars'");

		final var carGroupForM1 = findGroup(carCompanyMetaModel, "Car");
		final var nameAttribute = findAttribute(carGroupForM1, "name");
		final var brandAttribute = findAttribute(carGroupForM1, "brand");
		assertEquals("peugeot1", car.get(nameAttribute), "Car.name should be resolved");
		assertEquals("Peugeot", car.get(brandAttribute), "Car.brand should be resolved via DynamicModelPackage");
	}

	@Test
	void loadPeugeotM1Model_canResolveLocalReferenceWithDynamicMetaModel() throws IOException
	{
		final var metaLoader = LmLoader.withEmptyRegistry();
		final var metaSource = Files.readString(Path.of("src/test/model/CarCompany.lm"), StandardCharsets.UTF_8);
		final var metaDoc = metaLoader.loadModel(metaSource);
		assertTrue(metaDoc.diagnostics()
						  .stream()
						  .noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
				   "CarCompany meta-model diagnostics should not contain errors");

		final var carCompanyMetaModel = (MetaModel) metaDoc.model();
		final var registryBuilder = new ModelRegistry.Builder();
		registryBuilder.register(LMCoreModelPackage.MODEL);
		registryBuilder.register(carCompanyMetaModel);
		final var registry = registryBuilder.build();

		final var loader = new LmLoader(registry);
		final var source = Files.readString(Path.of("src/test/model/PeugeotWithReference.lm"), StandardCharsets.UTF_8);

		final var objects = loader.loadObjects(source);
		assertEquals(1, objects.size(), "PeugeotWithReference.lm should produce a single root object");

		final var root = (LMObject) objects.getFirst();
		final var carCompanyGroup = findGroup(carCompanyMetaModel, "CarCompany");
		final var ceoRelation = findRelation(carCompanyGroup, "ceo");
		final var parcsRelation = findRelation(carCompanyGroup, "parcs");

		final var ceo = (LMObject) root.get(ceoRelation);
		assertNotNull(ceo, "CarCompany.ceo should be present");

		@SuppressWarnings("unchecked")
		final var parcs = (java.util.List<LMObject>) root.get(parcsRelation);
		final var parc = parcs.getFirst();
		final var carParcGroup = findGroup(carCompanyMetaModel, "CarParc");
		final var carsRelation = findRelation(carParcGroup, "cars");
		@SuppressWarnings("unchecked")
		final var cars = (java.util.List<LMObject>) parc.get(carsRelation);
		final var car = cars.getFirst();

		final var personGroup = findGroup(carCompanyMetaModel, "Person");
		final var carRelation = findRelation(personGroup, "car");
		final var referencedCar = (LMObject) ceo.get(carRelation);
		assertSame(car, referencedCar, "Person.car should resolve to the local '@peugeot1' Car instance");
	}

	private static Group<LMObject> findGroup(final MetaModel metaModel, final String name)
	{
		@SuppressWarnings("unchecked")
		final var group = (Group<LMObject>) metaModel.groups()
													.stream()
													.filter(g -> name.equals(g.name()))
													.findFirst()
													.orElseThrow();
		return group;
	}

	@SuppressWarnings("unchecked")
	private static Relation<LMObject, ?, ?, ?> findRelation(final Group<LMObject> group,
															final String name)
	{
		return (Relation<LMObject, ?, ?, ?>) ModelUtil.streamAllFeatures(group)
													  .filter(Relation.class::isInstance)
													  .filter(f -> name.equals(f.name()))
													  .findFirst()
													  .orElseThrow();
	}

	@SuppressWarnings("unchecked")
	private static Attribute<Object, Object, ?, ?> findAttribute(final Group<LMObject> group, final String name)
	{
		return (Attribute<Object, Object, ?, ?>) ModelUtil.streamAllFeatures(group)
														 .filter(Attribute.class::isInstance)
														 .filter(f -> name.equals(f.name()))
														 .findFirst()
														 .orElseThrow();
	}
}
