package org.logoce.lmf.core.loader.linking.feature.reference;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.api.loader.LmLoader;
import org.logoce.lmf.core.api.loader.RelationReferenceCompletions;
import org.logoce.lmf.core.api.loader.model.LmDocument;
import org.logoce.lmf.core.api.text.syntax.PNode;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.api.loader.linking.LinkNode;
import test.model.carcompany.CarCompanyModelPackage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RelationReferenceCompletionsM1Test
{
	@Test
	void localCandidatesFromM1LinkTreesEvenWithoutOwningModel() throws Exception
	{
		final var registryBuilder = new ModelRegistry.Builder();
		registryBuilder.register(LMCoreModelPackage.MODEL);
		registryBuilder.register(CarCompanyModelPackage.MODEL);
		final var registry = registryBuilder.build();

		final var loader = new LmLoader(registry);
		final var source = Files.readString(
			Path.of("src/test/model/Peugeot.lm"), StandardCharsets.UTF_8);

		final LmDocument doc = loader.loadModel(source);
		assertFalse(doc.roots().isEmpty(), "Peugeot.lm should have at least one root");

		@SuppressWarnings("unchecked")
		final List<? extends LinkNode<?, PNode>> linkTrees =
			(List<? extends LinkNode<?, PNode>>) doc.linkTrees();
		assertFalse(linkTrees.isEmpty(), "Peugeot.lm should produce link trees");

		final MetaModel carCompanyMetaModel = CarCompanyModelPackage.MODEL;
		final Relation<?, ?, ?, ?> carRelation = findRelation(carCompanyMetaModel, "Person", "car");
		assertNotNull(carRelation, "Relation Person.car should be present in CarCompany meta-model");

		final var candidates = RelationReferenceCompletions.collectRelationCandidates(
			null,
			linkTrees,
			carRelation,
			registry);

		assertFalse(candidates.isEmpty(), "RelationReferenceCompletions should produce candidates for Person.car");
		assertTrue(candidates.stream().anyMatch(c -> "@peugeot1".equals(c.label()) && c.local()),
				   "Local candidate '@peugeot1' should be derived from M1 link trees even without owning model");
	}

	@Test
	void crossModelCandidatesFromMetamodelHeaderComeFromRegistry() throws Exception
	{
		final var registryBuilder = new ModelRegistry.Builder();
		registryBuilder.register(LMCoreModelPackage.MODEL);
		registryBuilder.register(CarCompanyModelPackage.MODEL);
		final var registry = registryBuilder.build();

		final var loader = new LmLoader(registry);
		final var source = Files.readString(
			Path.of("src/test/model/Peugeot.lm"), StandardCharsets.UTF_8);

		final LmDocument doc = loader.loadModel(source);
		assertFalse(doc.roots().isEmpty(), "Peugeot.lm should have at least one root");

		@SuppressWarnings("unchecked")
		final List<? extends LinkNode<?, PNode>> linkTrees =
			(List<? extends LinkNode<?, PNode>>) doc.linkTrees();
		assertFalse(linkTrees.isEmpty(), "Peugeot.lm should produce link trees");

		final MetaModel carCompanyMetaModel = CarCompanyModelPackage.MODEL;
		final Relation<?, ?, ?, ?> carRelation = findRelation(carCompanyMetaModel, "Person", "car");
		assertNotNull(carRelation, "Relation Person.car should be present in CarCompany meta-model");

		final var candidates = RelationReferenceCompletions.collectRelationCandidates(
			null,
			linkTrees,
			carRelation,
			registry);

		assertTrue(candidates.stream().anyMatch(c -> c.label().startsWith("#CarCompany@") && !c.local()),
				   "Cross-model candidates '#CarCompany@Type' should be derived from CarCompany meta-model via registry");
	}

	private static Relation<?, ?, ?, ?> findRelation(final MetaModel metaModel,
													 final String groupName,
													 final String relationName)
	{
		for (final Group<?> group : metaModel.groups())
		{
			if (!groupName.equals(group.name()))
			{
				continue;
			}

			for (final var feature : group.features())
			{
				if (feature instanceof Relation<?, ?, ?, ?> relation && relationName.equals(feature.name()))
				{
					return relation;
				}
			}
		}
		return null;
	}
}
