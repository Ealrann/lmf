package org.logoce.lmf.model.resource.linking;

import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.linking.feature.ITokenResolver;
import org.logoce.lmf.model.resource.linking.feature.NodeLinker;
import org.logoce.lmf.model.resource.linking.tree.LinkNode;
import org.logoce.lmf.model.resource.linking.tree.ResolvedNode;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TreeToFeatureLinker
{
	private final List<Relation<?, ?>> containmentRelations;

	private final Group<?> group;
	private final NodeLinker nodeLinker;

	public TreeToFeatureLinker(final Group<?> group)
	{
		this.group = group;
		final var features = group.features();
		final var wordResolvers = features.stream()
										  .map(ITokenResolver::buildResolver)
										  .filter(Optional::isPresent)
										  .map(Optional::get)
										  .toList();

		containmentRelations = features.stream()
									   .filter(Relation.class::isInstance)
									   .map(r -> (Relation<?, ?>) r)
									   .filter(Relation::contains)
									   .collect(Collectors.toUnmodifiableList());

		nodeLinker = new NodeLinker(wordResolvers);
	}

	public void resolve(final ResolvedNode<?, ?> node)
	{
		node.resolve(nodeLinker);
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
