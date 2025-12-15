package org.logoce.lmf.core.loader.internal.linking;

import org.logoce.lmf.core.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.internal.feature.ITokenResolver;
import org.logoce.lmf.core.api.util.ModelUtil;
import org.logoce.lmf.core.api.model.ModelRegistry;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TreeToFeatureLinker
{
	private final List<Relation<?, ?, ?, ?>> containmentRelations;
	private final Group<?> group;
	private final NodeLinker nodeLinker;

	public TreeToFeatureLinker(final Group<?> group, final ModelRegistry modelRegistry)
	{
		this.group = group;
		final List<Feature<?, ?, ?, ?>> allFeatures = ModelUtil.streamAllFeatures(group).toList();

		final var seen = Collections.newSetFromMap(new IdentityHashMap<Feature<?, ?, ?, ?>, Boolean>());
		final var features = allFeatures.stream().filter(seen::add).toList();
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

		containmentRelations = features.stream()
										  .filter(Relation.class::isInstance)
										  .map(r -> (Relation<?, ?, ?, ?>) r)
										  .filter(Relation::contains)
										  .collect(Collectors.toUnmodifiableList());

		nodeLinker = new NodeLinker(attributeResolvers, relationResolvers);
	}

	public void resolve(final LinkNodeInternal<?, ?, ?> linkNode)
	{
		linkNode.resolveReferences(nodeLinker);
	}

	public Stream<Relation<?, ?, ?, ?>> streamContainmentRelations()
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
