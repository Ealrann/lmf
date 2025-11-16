package org.logoce.lmf.model.resource.linking;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.linking.feature.ITokenResolver;
import org.logoce.lmf.model.resource.linking.linker.NodeLinker;
import org.logoce.lmf.model.resource.linking.tree.LinkNodeInternal;
import org.logoce.lmf.model.util.ModelRegistry;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TreeToFeatureLinker
{
	private final List<Relation<?, ?>> containmentRelations;

	private final Group<?> group;
	public final NodeLinker nodeLinker;

	public TreeToFeatureLinker(final Group<?> group, final ModelRegistry modelRegistry)
	{
		this.group = group;
		final var features = group.features();
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
									   .map(r -> (Relation<?, ?>) r)
									   .filter(Relation::contains)
									   .collect(Collectors.toUnmodifiableList());

		nodeLinker = new NodeLinker(attributeResolvers, relationResolvers);
	}

	public void resolve(final LinkNodeInternal<?, ?, ?> linkNode)
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
}
