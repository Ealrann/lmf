package org.logoce.lmf.model.resource.transform.word;

import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.transform.node.TreeBuilderNode;
import org.logoce.lmf.model.resource.transform.word.resolver.ITokenResolver;
import org.logoce.lmf.model.resource.transform.word.resolver.TokenResolver;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TreeToFeatureResolver
{
	private final List<Relation<?, ?>> containmentRelations;

	private final Group<?> group;
	private final TokenResolver tokenResolver;

	public TreeToFeatureResolver(final Group<?> group)
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

		tokenResolver = new TokenResolver(wordResolvers);
	}

	public void resolve(final TreeBuilderNode<?> node)
	{
		final var wordResolutions = tokenResolver.resolve(node);

		node.setResolutions(wordResolutions);
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
