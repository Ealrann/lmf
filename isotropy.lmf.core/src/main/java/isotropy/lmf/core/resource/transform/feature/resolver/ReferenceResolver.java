package isotropy.lmf.core.resource.transform.feature.resolver;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.model.ModelRegistry;
import isotropy.lmf.core.resource.transform.feature.IFeatureResolution;
import isotropy.lmf.core.resource.transform.node.BuilderNode;
import isotropy.lmf.core.resource.util.Tree;

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
	public Optional<IFeatureResolution> resolve(Tree<BuilderNode<?>> tree, String value)
	{
		if (value.startsWith("#"))
		{
			return resolveExternalDependency(value);
		}
		else if (value.startsWith("/"))
		{
			return resolveLocalDependency(tree, value);
		}
		else
		{
			return Optional.empty();
		}
	}

	private Optional<IFeatureResolution> resolveLocalDependency(final Tree<BuilderNode<?>> tree, final String uri)
	{
		final var path = uri.substring(1);
		final var steps = path.split("/");

		Tree<BuilderNode<?>> current = tree.root();
		for (final var step : steps)
		{
			final var pointIndex = step.indexOf('.');
			final var featureName = pointIndex == -1 ? step : step.substring(0, pointIndex);
			final Integer index = pointIndex == -1 ? null : Integer.valueOf(step.substring(pointIndex + 1));

			//current.data().featureResolutions
		}

		return Optional.empty();
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

		if (isSubGroup(feature.group(), current.lmGroup()))
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

	private static boolean isSubGroup(final Group<?> parent, final Group<?> check)
	{
		if (check == parent)
		{
			return true;
		}
		else if (check.includes()
					  .isEmpty() == false)
		{
			for (final var include : check.includes())
			{
				if (isSubGroup(parent, include))
				{
					return true;
				}
			}
		}
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
}
