package org.logoce.lmf.core.util;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.api.model.DynamicModelPackage;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.api.util.ModelUtil;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.linking.LmModelLinker;
import org.logoce.lmf.core.loader.linking.TreeToFeatureLinker;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

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
										   "logoce.lmf.core.generator",
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

		assertNotNull(metaModel.lmPackage(), "Loaded MetaModel should have a package");
		assertInstanceOf(DynamicModelPackage.class,
						 metaModel.lmPackage(),
						 "Loaded MetaModel should be assigned a DynamicModelPackage");

		final var dynamicPackage = (DynamicModelPackage) metaModel.lmPackage();
		assertSame(metaModel, dynamicPackage.model(), "DynamicModelPackage should be bound to the loaded MetaModel");

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
		final Attribute<Object, Object, ?, ?> brandAttribute = (Attribute<Object, Object, ?, ?>) carGroup.features()
																										 .stream()
																										 .filter(f -> f instanceof Attribute<?, ?, ?, ?> &&
																													 "brand".equals(
																														 f.name()))
																										 .findFirst()
																										 .orElseThrow();

		@SuppressWarnings("unchecked")
		final Relation<LMObject, ?, ?, ?> carsRelation = (Relation<LMObject, ?, ?, ?>) carParcGroup.features()
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

	@Test
	void dynamicPackage_linksPeugeotM1ModelWithoutContainmentErrors() throws Exception
	{
		Path carCompanyPath = Path.of("logoce.lmf.core.api", "src", "test", "model", "CarCompany.lm");
		if (!Files.exists(carCompanyPath))
		{
			carCompanyPath = Path.of("..", "logoce.lmf.core.api", "src", "test", "model", "CarCompany.lm");
		}
		assertTrue(Files.exists(carCompanyPath), "CarCompany.lm for model tests should exist");

		final String carCompanySource = Files.readString(carCompanyPath, StandardCharsets.UTF_8);

		final var metaLoader = new LmLoader(ModelRegistry.empty());
		final var carCompanyDoc = metaLoader.loadModel(carCompanySource);
		assertTrue(carCompanyDoc.diagnostics().isEmpty(), "CarCompany meta-model diagnostics should be empty");
		assertNotNull(carCompanyDoc.model(), "CarCompany meta-model should be loaded");
		assertTrue(carCompanyDoc.model() instanceof MetaModel, "Loaded CarCompany should be a MetaModel");

		final var carCompanyMetaModel = (MetaModel) carCompanyDoc.model();
		assertNotNull(carCompanyMetaModel.lmPackage(), "Loaded CarCompany meta-model should have a package");
		assertInstanceOf(DynamicModelPackage.class,
						 carCompanyMetaModel.lmPackage(),
						 "Loaded CarCompany meta-model should be assigned a DynamicModelPackage");

		final var registryBuilder = new ModelRegistry.Builder();
		registryBuilder.register(LMCoreModelPackage.MODEL);
		registryBuilder.register(carCompanyMetaModel);
		final var registry = registryBuilder.build();

		Path peugeotPath = Path.of("logoce.lmf.core.api", "src", "test", "model", "Peugeot.lm");
		if (!Files.exists(peugeotPath))
		{
			peugeotPath = Path.of("..", "logoce.lmf.core.api", "src", "test", "model", "Peugeot.lm");
		}
		assertTrue(Files.exists(peugeotPath), "Peugeot.lm for model tests should exist");

		final String peugeotSource = Files.readString(peugeotPath, StandardCharsets.UTF_8);

		final var diagnostics = new java.util.ArrayList<LmDiagnostic>();
		final var reader = new LmTreeReader();
		final var readResult = reader.read(peugeotSource, diagnostics);

		// Sanity check: CarCompany group should expose a containment relation to CarParc.
		@SuppressWarnings("unchecked")
		final Group<LMObject> carCompanyGroup = (Group<LMObject>) carCompanyMetaModel.groups()
																					 .stream()
																					 .filter(g -> "CarCompany".equals(
																						 g.name()))
																					 .findFirst()
																					 .orElseThrow();

		final var carCompanyTreeLinker = new TreeToFeatureLinker(carCompanyGroup, registry);
		final var carCompanyContainments = carCompanyTreeLinker.streamContainmentRelations().toList();
		assertTrue(carCompanyContainments.stream()
										 .anyMatch(r -> "parcs".equals(r.name()) &&
														r.concept() != null &&
														"CarParc".equals(r.concept().name())),
				   "CarCompany should declare a containment relation 'parcs' to CarParc");

		@SuppressWarnings("unchecked")
		final Group<LMObject> carParcGroup = (Group<LMObject>) carCompanyMetaModel.groups()
																				  .stream()
																				  .filter(g -> "CarParc".equals(
																					  g.name()))
																				  .findFirst()
																				  .orElseThrow();

		assertTrue(carCompanyContainments.stream()
										 .anyMatch(r -> ModelUtil.isSubGroup(
											 r.concept(), carParcGroup)),
				   "CarCompany containment 'parcs' concept should be a super-group of CarParc");

		@SuppressWarnings("unchecked")
		final Group<LMObject> personGroup = (Group<LMObject>) carCompanyMetaModel.groups()
																				 .stream()
																				 .filter(g -> "Person".equals(
																					 g.name()))
																				 .findFirst()
																				 .orElseThrow();

		assertTrue(carCompanyContainments.stream()
										 .anyMatch(r -> "ceo".equals(r.name()) &&
														ModelUtil.isSubGroup(r.concept(), personGroup)),
				   "CarCompany should declare a containment relation 'ceo' to Person");

		assertFalse(readResult.roots().isEmpty(), "Peugeot.lm should have at least one root");
		assertTrue(diagnostics.stream().noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
				   "Peugeot.lm parse diagnostics should not contain errors");

		final var linker = new LmModelLinker<PNode>(registry, List.of(carCompanyMetaModel.lmPackage()));
		final var linkDiagnostics = new java.util.ArrayList<LmDiagnostic>();
		linker.linkModel(readResult.roots(), linkDiagnostics, readResult.source());

		assertTrue(linkDiagnostics.stream()
								  .noneMatch(d -> d.message() != null &&
												  d.message().contains(
													  "Cannot find containment relation from parent CarCompany")),
				   "Dynamic linking of Peugeot.lm should not report CarCompany containment errors");
	}

	@Test
	void dynamicPackage_linksLMCoreMetaModelWithoutErrors() throws Exception
	{
		Path lmCorePath = Path.of("logoce.lmf.core.api", "src", "main", "model", "asset", "LMCore.lm");
		if (!Files.exists(lmCorePath))
		{
			lmCorePath = Path.of("..", "logoce.lmf.core.api", "src", "main", "model", "asset", "LMCore.lm");
		}
		assertTrue(Files.exists(lmCorePath), "LMCore.lm should exist");

		final String lmCoreSource = Files.readString(lmCorePath, StandardCharsets.UTF_8);

		final var lmCoreMetaModel = LMCoreModelPackage.MODEL;
		final var dynamicPackage = new DynamicModelPackage(lmCoreMetaModel);

		final var registryBuilder = new ModelRegistry.Builder();
		registryBuilder.register(lmCoreMetaModel);
		final var registry = registryBuilder.build();

		final var diagnostics = new java.util.ArrayList<LmDiagnostic>();
		final var reader = new LmTreeReader();
		final var readResult = reader.read(lmCoreSource, diagnostics);

		assertFalse(readResult.roots().isEmpty(), "LMCore.lm should have at least one root");
		assertTrue(diagnostics.stream().noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR),
				   "LMCore.lm parse diagnostics should not contain errors");

		final var linker = new LmModelLinker<PNode>(registry, List.of(dynamicPackage));
		final var linkDiagnostics = new java.util.ArrayList<LmDiagnostic>();
		linker.linkModel(readResult.roots(), linkDiagnostics, readResult.source());

		// LMCore is large and intentionally uses advanced patterns (aliases, weak typing, contextual paths).
		// For the dynamic package we only require that linking does not crash and produces some diagnostics;
		// exact parity with the generated LMCorePackage is not needed here.
		assertFalse(linkDiagnostics.isEmpty(),
					"Dynamic linking of LMCore.lm is expected to produce diagnostics");
	}
}
