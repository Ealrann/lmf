package org.logoce.lmf.model.resource.linking.feature.reference;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.resource.linking.FeatureResolution;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.ModelUtils;

import java.util.List;
import java.util.Optional;

public class ModelReferenceResolver implements ReferenceResolver
{
	public final Model model;
	private final Relation<?, ?> relation;

	public ModelReferenceResolver(final String modelName, final Relation<?, ?> relation)
	{
		this.model = ModelRegistry.Instance.getModel(modelName);
		this.relation = relation;
	}

	@Override
	public Optional<FeatureResolution<Relation<?, ?>>> resolve(final PathParser pathParser)
	{
		final var firstStep = pathParser.next();

		if (firstStep.type() == PathParser.Type.CHILD)
		{
			final var explorer = new ModelExplorer(model);
			pathParser.rewind();
			while (pathParser.hasNext())
			{
				final var next = pathParser.next();
				assert next.type() == PathParser.Type.CHILD;
				explorer.exploreNode(next.text());
			}
			return explorer.build(relation);
		}
		else if (firstStep.type() == PathParser.Type.NAME)
		{
			final var name = firstStep.text();
			return resolveName(name, relation);
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> Optional<FeatureResolution<Relation<?, ?>>> resolveName(final String name,
																						 final Relation<T, ?> relation)
	{
		final var group = (Group<?>) relation.reference().group();
		return ModelUtils.streamTree(model)
						 .filter(o -> ModelUtils.isSubGroup(group, o.lmGroup()))
						 .map(o -> (T) o)
						 .filter(o -> o.get(LMCoreDefinition.Features.NAMED.NAME).equals(name))
						 .findAny()
						 .map(o -> new ModelExplorer.StaticReferenceResolution<>(relation, o));
	}

	private static final class ModelExplorer
	{
		private LMObject current;

		public ModelExplorer(final LMObject start)
		{
			this.current = start;
		}

		@SuppressWarnings("unchecked")
		public void exploreNode(final String step)
		{
			final var pointIndex = step.indexOf('.');
			final var featureName = pointIndex == -1 ? step : step.substring(0, pointIndex);
			final Integer index = pointIndex == -1 ? null : Integer.valueOf(step.substring(pointIndex + 1));

			final var feature = current.lmGroup()
									   .features()
									   .stream()
									   .filter(f -> f.name().equals(featureName))
									   .findAny()
									   .orElseThrow();

			if (index != null)
			{
				final var featureValue = current.get((Relation<?, List<? extends LMObject>>) feature);
				current = featureValue.get(index);
			}
			else
			{
				final var featureValue = current.get((Relation<?, ? extends LMObject>) feature);
				current = featureValue;
			}
		}

		public <T extends Relation<?, ?>> Optional<FeatureResolution<Relation<?, ?>>> build(final T relation)
		{
			if (ModelUtils.isSubGroup(relation.reference().group(), current.lmGroup()))
			{
				return Optional.of(buildInternal(relation, current));
			}
			else
			{
				return Optional.empty();
			}
		}

		@SuppressWarnings("unchecked")
		private static <Y extends LMObject> StaticReferenceResolution<Y, Relation<Y, ?>> buildInternal(final Relation<?, ?> relation,
																									   final Y object)
		{
			return new StaticReferenceResolution<>((Relation<Y, ?>) relation, object);
		}

		public record StaticReferenceResolution<Y extends LMObject, T extends Relation<Y, ?>>(T relation,
																							  Y value) implements
																									   FeatureResolution<Relation<?, ?>>
		{
			@Override
			public void pushValue(final IFeaturedObject.Builder<?> builder)
			{
				builder.push(relation, () -> value);
			}

			@Override
			public T feature()
			{
				return relation;
			}
		}
	}
}
