package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.linking.FeatureResolution;
import org.logoce.lmf.model.resource.linking.tree.LinkNodeInternal;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.ModelUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public final class ReferenceResolver<T extends LMObject> extends AbstractResolver<T, Relation<T, ?>> implements
																									 ITokenResolver<T>
{
	public ReferenceResolver(final Relation<T, ?> relation)
	{
		super(relation);
		assert !relation.contains();
	}

	@Override
	protected Optional<FeatureResolution> internalResolve(LinkNodeInternal<?, ?> node, String value)
	{
		if (value.startsWith("#"))
		{
			return resolveExternalDependency(value);
		}
		else if (value.startsWith("/") || value.startsWith("."))
		{
			return resolveLocalDependency(node, value);
		}
		else
		{
			return Optional.empty();
		}
	}

	@SuppressWarnings({"ReassignedVariable", "unchecked"})
	private Optional<FeatureResolution> resolveLocalDependency(final LinkNodeInternal<?, ?> node, final String uri)
	{
		LinkNodeInternal<?, ?> current = node;
		if (uri.startsWith("/"))
		{
			current = current.root();
		}

		final var steps = uri.split("/");
		for (final var step : steps)
		{
			if (step.isEmpty() || step.equals("."))
			{
				continue;
			}
			if (step.equals(".."))
			{
				current = current.parent();
			}
			else
			{
				final var pointIndex = step.indexOf('.');
				final var featureName = pointIndex == -1 ? step : step.substring(0, pointIndex);
				final int index = pointIndex == -1 ? 0 : Integer.parseInt(step.substring(pointIndex + 1));

				final var children = current.streamChildren()
											.filter(c -> c.containingRelation().name().equals(featureName))
											.toList();
				if (children.size() < index + 1)
				{
					throw new NoSuchElementException("Cannot resolve path " + uri);
				}
				current = children.get(index);
			}
		}

		if (ModelUtils.isSubGroup(feature.reference().group(), current.group()))
		{
			return Optional.of(new DynamicReferenceResolution<>(feature, (LinkNodeInternal<T, ?>) current));
		}
		else
		{
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private Optional<FeatureResolution> resolveExternalDependency(final String value)
	{
		final var uri = value.substring(1);
		final var firstSlashIndex = uri.indexOf('/');

		final var modelName = uri.substring(0, firstSlashIndex);
		final var path = uri.substring(firstSlashIndex + 1);
		final var model = ModelRegistry.Instance.get(modelName).model();

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

		if (ModelUtils.isSubGroup(feature.reference().group(), current.lmGroup()))
		{
			return Optional.of(new StaticReferenceResolution<>(feature, (T) current));
		}
		else
		{
			return Optional.empty();
		}
	}

	public record StaticReferenceResolution<T extends LMObject>(Relation<T, ?> relation, T value) implements
																								  FeatureResolution
	{
		@Override
		public void pushValue(final IFeaturedObject.Builder<?> builder)
		{
			builder.push(relation, () -> value);
		}
	}

	public record DynamicReferenceResolution<T extends LMObject>(Relation<T, ?> relation, LinkNodeInternal<T, ?> linkNode) implements
																														   FeatureResolution
	{
		@Override
		public void pushValue(final IFeaturedObject.Builder<?> builder)
		{
			builder.push(relation, linkNode::build);
		}
	}
}
