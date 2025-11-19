package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.linking.FeatureResolution;
import org.logoce.lmf.model.resource.linking.feature.reference.LocalReferenceExplorer;
import org.logoce.lmf.model.resource.linking.feature.reference.ModelReferenceResolver;
import org.logoce.lmf.model.resource.linking.feature.reference.PathParser;
import org.logoce.lmf.model.resource.linking.feature.reference.ReferenceResolver;
import org.logoce.lmf.model.resource.linking.tree.LinkNodeInternal;
import org.logoce.lmf.model.resource.transform.LinkNode;
import org.logoce.lmf.model.util.ModelRegistry;

import java.util.List;
import java.util.Optional;

public final class RelationResolver extends AbstractResolver<Relation<?, ?>>
{
	private final ModelRegistry modelRegistry;

	public RelationResolver(final Relation<?, ?> relation, final ModelRegistry modelRegistry)
	{
		super(relation);
		this.modelRegistry = modelRegistry;
		assert !relation.contains();
	}

	public Optional<FeatureResolution<Relation<?, ?>>> resolve(LinkNodeInternal<?, ?, ?> node, List<String> values)
	{
		return super.resolve(values, value -> internalResolve(node, value));
	}

	private Optional<FeatureResolution<Relation<?, ?>>> internalResolve(LinkNodeInternal<?, ?, ?> node, String value)
	{
		final var parser = new PathParser(value);
		final var resolver = buildReferenceResolver(node, parser);
		return resolver.resolve(parser);
	}

	private ReferenceResolver buildReferenceResolver(final LinkNodeInternal<?, ?, ?> node, final PathParser parser)
	{
		final var firstStep = parser.next();
		if (firstStep.type() == PathParser.Type.MODEL)
		{
			final var modelName = firstStep.text();
			final var model = modelRegistry.getModel(modelName);
			if (model == null)
			{
				final var available = modelRegistry.models().map(m -> m.name()).toList();
				throw new AssertionError("Cannot resolve model '" + modelName + "' in registry. Available models: " +
										 available);
			}
			return new ModelReferenceResolver(model, feature);
		}
		else
		{
			parser.rewind();
			return new LocalReferenceExplorer(node, feature);
		}
	}

	public record DynamicReferenceResolution<T extends LMObject>(Relation<T, ?> relation,
																 LinkNode<T, ?> linkNode) implements FeatureResolution<Relation<?, ?>>
	{
		@Override
		public void pushValue(final IFeaturedObject.Builder<?> builder)
		{
			builder.push(relation, linkNode::build);
		}

		@Override
		public Relation<T, ?> feature()
		{
			return relation;
		}
	}
}
