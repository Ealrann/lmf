package isotropy.lmf.core.resource.transform.word.resolver;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.model.ModelRegistry;
import isotropy.lmf.core.resource.transform.node.TreeBuilderNode;
import isotropy.lmf.core.resource.transform.word.IFeatureResolution;
import isotropy.lmf.core.util.ModelUtils;

import java.util.List;
import java.util.Optional;

public final class ReferenceResolver<T extends LMObject> extends AbstractResolver<T, Relation<T, ?>> implements
																									 IWordResolver<T>
{
	public ReferenceResolver(final Relation<T, ?> relation)
	{
		super(relation);
		assert !relation.contains();
	}

	@Override
	public Optional<IFeatureResolution> resolve(TreeBuilderNode<?> node, String value)
	{
		if (value.startsWith("#"))
		{
			return resolveExternalDependency(value);
		}
		else if (value.startsWith("/"))
		{
			return resolveLocalDependency(node, value);
		}
		else
		{
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private Optional<IFeatureResolution> resolveLocalDependency(final TreeBuilderNode<?> node, final String uri)
	{
		final var path = uri.substring(1);
		final var steps = path.split("/");

		TreeBuilderNode<?> current = node.root();
		for (final var step : steps)
		{
			final var pointIndex = step.indexOf('.');
			final var featureName = pointIndex == -1 ? step : step.substring(0, pointIndex);
			final int index = pointIndex == -1 ? 0 : Integer.parseInt(step.substring(pointIndex + 1));

			final var children = current.children()
										.stream()
										.filter(c -> c.containingRelation()
													  .name()
													  .equals(featureName))
										.toList();
			current = children.get(index);
		}

		if (ModelUtils.isSubGroup(feature.groupReference()
										 .group(),
								  current.modelGroup()
										 .group()))
		{
			return Optional.of(new DynamicReferenceResolution<>(feature, (TreeBuilderNode<T>) current));
		}
		else
		{
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private Optional<IFeatureResolution> resolveExternalDependency(final String value)
	{
		final var uri = value.substring(1);
		final var firstSlashIndex = uri.indexOf('/');

		final var modelName = uri.substring(0, firstSlashIndex);
		final var path = uri.substring(firstSlashIndex + 1);
		final var model = ModelRegistry.Instance.get(modelName)
												.model();

		//TODO this code should be done only once, earlier (word pre-resolution)
		LMObject current = model;
		final var steps = path.split("/");
		for (final var step : steps)
		{
			final var pointIndex = step.indexOf('.');
			final var featureName = pointIndex == -1 ? step : step.substring(0, pointIndex);
			final Integer index = pointIndex == -1 ? null : Integer.valueOf(step.substring(pointIndex + 1));

			final var feature = current.lmGroup()
									   .features()
									   .stream()
									   .filter(f -> f.name()
													 .equals(featureName))
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

		if (ModelUtils.isSubGroup(feature.groupReference()
										 .group(), current.lmGroup()))
		{
			return Optional.of(new StaticReferenceResolution<>(feature, (T) current));
		}
		else
		{
			return Optional.empty();
		}
	}

	@Override
	public boolean isBooleanAttribute()
	{
		return false;
	}

	public static final class StaticReferenceResolution<T extends LMObject> implements IFeatureResolution
	{
		final Relation<T, ?> relation;
		final T value;

		private StaticReferenceResolution(final Relation<T, ?> relation, final T value)
		{
			this.relation = relation;
			this.value = value;
		}

		@Override
		public void pushValue(final IFeaturedObject.Builder<?> builder)
		{
			builder.push(relation, () -> value);
		}
	}

	public static final class DynamicReferenceResolution<T extends LMObject> implements IFeatureResolution
	{
		final Relation<T, ?> relation;
		final TreeBuilderNode<T> value;

		private DynamicReferenceResolution(final Relation<T, ?> relation, final TreeBuilderNode<T> value)
		{
			this.relation = relation;
			this.value = value;
		}

		@Override
		public void pushValue(final IFeaturedObject.Builder<?> builder)
		{
			builder.push(relation, value::build);
		}
	}
}
