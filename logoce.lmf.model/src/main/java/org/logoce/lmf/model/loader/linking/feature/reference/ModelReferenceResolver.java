package org.logoce.lmf.model.loader.linking.feature.reference;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Named;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.loader.linking.FeatureResolution;
import org.logoce.lmf.model.util.ModelUtil;

import java.util.List;
import java.util.Optional;

public final class ModelReferenceResolver implements ReferenceResolver
{
	private final Model model;
	private final Relation<?, ?, ?, ?> relation;

	/**
	 * Marker interface for static model reference resolutions that point to a
	 * concrete target {@link LMObject}. This is used by tooling-oriented
	 * consumers (for example the semantic index builder) to extract the target
	 * object without depending on the internal {@link ModelExplorer} type.
	 */
	public interface StaticResolution extends FeatureResolution<Relation<?, ?, ?, ?>>
	{
		LMObject value();
	}

	public ModelReferenceResolver(final Model model, final Relation<?, ?, ?, ?> relation)
	{
		this.model = model;
		this.relation = relation;
	}

	@Override
	public Optional<FeatureResolution<Relation<?, ?, ?, ?>>> resolve(final PathParser pathParser)
	{
		final var firstStep = pathParser.next();

		if (firstStep.type() == PathParser.Type.CHILD)
		{
			final var explorer = new ModelExplorer(model);
			pathParser.rewind();
			while (pathParser.hasNext())
			{
				final var next = pathParser.next();
				if (next.type() != PathParser.Type.CHILD)
				{
					throw new IllegalArgumentException("Unexpected step in model path: " + next.type());
				}
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
			throw new IllegalArgumentException("Unsupported first step in model reference: " + firstStep.type());
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> Optional<FeatureResolution<Relation<?, ?, ?, ?>>> resolveName(final String name,
																							 final Relation<T, ?, ?, ?> relation)
	{
		final var group = (Group<?>) relation.concept();
		return ModelUtil.streamTree(model)
						.filter(o -> ModelUtil.isSubGroup(group, o.lmGroup()))
						.map(o -> (T) o)
						.filter(o -> {
							if (o instanceof Named named)
							{
								final var v = named.name();
								return v != null && v.equals(name);
							}
							return false;
						})
						.findAny()
						.map(o -> new ModelExplorer.StaticReferenceResolution<>(relation, o));
	}

	private static final class ModelExplorer
	{
		private LMObject current;

		ModelExplorer(final LMObject start)
		{
			this.current = start;
		}

		@SuppressWarnings("unchecked")
		void exploreNode(final String step)
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
				@SuppressWarnings("unchecked")
				final var featureValue = (List<? extends LMObject>) current.get(
						(Relation<?, List<? extends LMObject>, ?, ?>) feature);
				current = featureValue.get(index);
			}
			else
			{
				final var featureValue = (LMObject) current.get(
						(Relation<?, ? extends LMObject, ?, ?>) feature);
				current = featureValue;
			}
		}

		@SuppressWarnings("unchecked")
		public <T extends Relation<?, ?, ?, ?>> Optional<FeatureResolution<Relation<?, ?, ?, ?>>> build(final T relation)
		{
			final var concept = relation.concept();
			if (concept instanceof Group<?> group && ModelUtil.isSubGroup(group, current.lmGroup()))
			{
				return Optional.of(buildInternal(relation, current));
			}
			else
			{
				return Optional.empty();
			}
		}

		private static <Y extends LMObject> StaticReferenceResolution<Y, Relation<Y, ?, ?, ?>> buildInternal(final Relation<?, ?, ?, ?> relation,
																									   final Y object)
		{
			@SuppressWarnings("unchecked")
			final var typedRelation = (Relation<Y, ?, ?, ?>) relation;
			return new StaticReferenceResolution<>(typedRelation, object);
		}

		public record StaticReferenceResolution<Y extends LMObject, T extends Relation<Y, ?, ?, ?>>(T relation,
																									Y value) implements
																									   FeatureResolution<Relation<?, ?, ?, ?>>,
																									   StaticResolution
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
