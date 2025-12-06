package org.logoce.lmf.model.loader.linking;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.loader.linking.feature.ITokenResolver;
import org.logoce.lmf.model.loader.linking.linker.NodeLinker;
import org.logoce.lmf.model.util.ModelUtils;
import org.logoce.lmf.model.util.ModelRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TreeToFeatureLinker
{
	private final List<Relation<?, ?>> containmentRelations;
	private final Group<?> group;
	private final NodeLinker nodeLinker;

	public TreeToFeatureLinker(final Group<?> group, final ModelRegistry modelRegistry)
	{
		this.group = group;
		final List<Feature<?, ?>> allFeatures = ModelUtils.streamAllFeatures(group).toList();

		final Map<Object, Feature<?, ?>> featureMap = new LinkedHashMap<>();
		for (final Feature<?, ?> feature : allFeatures)
		{
			final Object key = feature.rawFeature() != null ? feature.rawFeature() : feature;
			featureMap.put(key, feature);
		}

		final var features = featureMap.values().stream().toList();
		final var tokenResolverBuilder = new ITokenResolver.Builder(modelRegistry);

		final var attributeResolvers = features.stream()
											   .filter(Attribute.class::isInstance)
											   .map(Attribute.class::cast)
											   .map(ITokenResolver.Builder::buildAttributeResolver)
											   .filter(Optional::isPresent)
											   .map(Optional::get)
											   .toList();

		final var relationResolvers = features.stream()
											  .filter(Relation.class::isInstance)
											  .map(Relation.class::cast)
											  .map(tokenResolverBuilder::buildRelationResolver)
											  .filter(Optional::isPresent)
											  .map(Optional::get)
											  .toList();

		containmentRelations = allFeatures.stream()
										  .filter(Relation.class::isInstance)
										  .map(r -> (Relation<?, ?>) r)
										  .filter(Relation::contains)
										  .collect(Collectors.toUnmodifiableList());

		nodeLinker = new NodeLinker(attributeResolvers, relationResolvers);
	}

	public void resolve(final org.logoce.lmf.model.loader.linking.tree.LinkNodeInternal<?, ?, ?> linkNode)
	{
		linkNode.resolveReferences(nodeLinker);
	}

	public Stream<Relation<?, ?>> streamContainmentRelations()
	{
		return containmentRelations.stream();
	}

	public Group<?> group()
	{
		return group;
	}

	public NodeLinker nodeLinker()
	{
		return nodeLinker;
	}
}
