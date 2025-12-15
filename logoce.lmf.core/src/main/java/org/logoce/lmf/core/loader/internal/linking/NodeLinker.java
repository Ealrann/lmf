package org.logoce.lmf.core.loader.internal.linking;

import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.linking.ResolutionAttempt;
import org.logoce.lmf.core.loader.internal.feature.AttributeResolver;
import org.logoce.lmf.core.loader.internal.feature.RelationResolver;
import org.logoce.lmf.core.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.internal.interpretation.PFeature;

import java.util.List;

public final class NodeLinker
{
	private final List<AttributeResolver> attributeResolvers;
	private final List<RelationResolver> relationResolvers;

	public NodeLinker(final List<AttributeResolver> attributeResolvers,
					  final List<RelationResolver> relationResolvers)
	{
		this.attributeResolvers = List.copyOf(attributeResolvers);
		this.relationResolvers = List.copyOf(relationResolvers);
	}

	public List<ResolutionAttempt<Attribute<?, ?, ?, ?>>> resolveAttributes(final List<PFeature> features)
	{
		final var runner = new TokenResolver<>(attributeResolvers, AttributeResolver::resolve);
		final var stream = features.stream().filter(PFeature::isAttribute);
		final var batchResolver = new BatchResolver<>(runner);
		return batchResolver.resolve(stream);
	}

	public List<ResolutionAttempt<Relation<?, ?, ?, ?>>> resolveRelations(final LinkNodeInternal<?, ?, ?> node)
	{
		final var runner = new TokenResolver<>(relationResolvers, (r, value) -> r.resolve(node, value));
		final var stream = node.features().stream().filter(PFeature::isRelation);
		final var batchResolver = new BatchResolver<>(runner);
		return batchResolver.resolve(stream);
	}
}
