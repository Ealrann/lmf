package org.logoce.lmf.model.resource.linking.linker;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.linking.feature.AttributeResolver;
import org.logoce.lmf.model.resource.linking.feature.RelationResolver;
import org.logoce.lmf.model.resource.linking.linker.internal.BatchResolver;
import org.logoce.lmf.model.resource.linking.linker.internal.TokenResolver;
import org.logoce.lmf.model.resource.linking.tree.LinkNodeInternal;
import org.logoce.lmf.model.resource.transform.ResolutionAttempt;

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

	public List<ResolutionAttempt<Attribute<?, ?>>> resolveAttributes(final List<PFeature> features)
	{
		final var runner = new TokenResolver<>(attributeResolvers, AttributeResolver::resolve);
		final var stream = features.stream().filter(PFeature::isAttribute);
		final var batchResolver = new BatchResolver<>(runner);
		return batchResolver.resolve(stream);
	}

	public List<ResolutionAttempt<Relation<?, ?>>> resolveRelations(final LinkNodeInternal<?, ?, ?> node)
	{
		final var runner = new TokenResolver<>(relationResolvers, (r, value) -> r.resolve(node, value));
		final var stream = node.features().stream().filter(PFeature::isRelation);
		final var batchResolver = new BatchResolver<>(runner);
		return batchResolver.resolve(stream);
	}
}
