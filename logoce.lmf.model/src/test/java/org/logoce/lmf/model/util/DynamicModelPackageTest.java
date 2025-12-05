package org.logoce.lmf.model.util;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.loader.LmLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class DynamicModelPackageTest
{
	@Test
	void dynamicBuilder_buildsObjectsWithAttributesAndRelations() throws IOException
	{
		final var carCompanyPath = Path.of("..",
										   "logoce.lmf.generator",
										   "src",
										   "test",
										   "model",
										   "CarCompany.lm");
		assertTrue(Files.exists(carCompanyPath), "CarCompany.lm for generator tests should exist");

		final String source = Files.readString(carCompanyPath, StandardCharsets.UTF_8);

		final var loader = LmLoader.withEmptyRegistry();
		final var document = loader.loadModel(source);
		assertTrue(document.diagnostics().isEmpty(), "Meta-model diagnostics should be empty");
		assertNotNull(document.model(), "Meta-model should be loaded");
		assertTrue(document.model() instanceof MetaModel, "Loaded model should be a MetaModel");

		final var metaModel = (MetaModel) document.model();

		// Build a dynamic package for this meta-model (no generated Package on this classpath).
		final var dynamicPackage = new DynamicModelPackage(metaModel);

		@SuppressWarnings("unchecked")
		final Group<LMObject> carGroup = (Group<LMObject>) metaModel.groups()
																   .stream()
																   .filter(g -> "Car".equals(g.name()))
																   .findFirst()
																   .orElseThrow();

		@SuppressWarnings("unchecked")
		final Group<LMObject> carParcGroup = (Group<LMObject>) metaModel.groups()
																	   .stream()
																	   .filter(g -> "CarParc".equals(g.name()))
																	   .findFirst()
																	   .orElseThrow();

		// Car has an attribute 'brand' and CarParc has a containment relation 'cars'.
		@SuppressWarnings("unchecked")
		final Attribute<Object, ?> brandAttribute = (Attribute<Object, ?>) carGroup.features()
																				   .stream()
																				   .filter(f -> f instanceof Attribute<?, ?> &&
																								 "brand".equals(
																									 f.name()))
																				   .findFirst()
																				   .orElseThrow();

		@SuppressWarnings("unchecked")
		final Relation<LMObject, ?> carsRelation = (Relation<LMObject, ?>) carParcGroup.features()
																					   .stream()
																					   .filter(f -> "cars".equals(
																						   f.name()))
																					   .findFirst()
																					   .orElseThrow();

		// Build a Car instance with a brand.
		final var carBuilder = dynamicPackage.builder(carGroup).orElseThrow();
		carBuilder.push(brandAttribute, "Peugeot");
		final LMObject car = carBuilder.build();

		assertEquals("Peugeot", car.get(brandAttribute), "Attribute value should round-trip");

		// Build a CarParc containing the Car.
		final var parcBuilder = dynamicPackage.builder(carParcGroup).orElseThrow();
		parcBuilder.push(carsRelation, () -> car);
		final LMObject parc = parcBuilder.build();

		@SuppressWarnings("unchecked")
		final List<LMObject> cars = (List<LMObject>) parc.get(carsRelation);
		assertEquals(1, cars.size(), "Containment relation should contain one car");
		assertSame(car, cars.getFirst(), "Contained car should be the one we added");

		assertSame(parc, car.lmContainer(), "Containment should set lmContainer on child");
		assertEquals(carsRelation, car.lmContainingFeature(), "Containment should set lmContainingFeature");
	}
}
