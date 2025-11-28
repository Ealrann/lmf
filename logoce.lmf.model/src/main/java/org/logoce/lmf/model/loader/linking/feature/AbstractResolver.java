package org.logoce.lmf.model.loader.linking.feature;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.loader.linking.FeatureResolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractResolver<T extends Feature<?, ?>> implements ITokenResolver
{
	protected final T feature;

	protected AbstractResolver(final T feature)
	{
		this.feature = feature;
	}

	@Override
	public final boolean match(final String featureName)
	{
		return feature.name().equals(featureName);
	}

	protected final Optional<FeatureResolution<T>> resolve(final List<String> values,
														   final Function<String, Optional<FeatureResolution<T>>> valueResolver)
	{
		if (values.size() > 1 && !feature.many())
		{
			return Optional.empty();
		}

		final List<FeatureResolution<T>> resolutions = new ArrayList<>();
		for (var value : values)
		{
			final var resolution = valueResolver.apply(value);
			if (resolution.isEmpty())
			{
				return Optional.empty();
			}
			else
			{
				resolutions.add(resolution.get());
			}
		}

		if (resolutions.size() > 1)
		{
			return Optional.of(new MultipleResolution<>(resolutions, feature));
		}
		else
		{
			return Optional.of(resolutions.getFirst());
		}
	}

	public static final class MultipleResolution<T extends Feature<?, ?>> implements FeatureResolution<T>
	{
		private final List<FeatureResolution<T>> resolutions;
		private final T feature;

		public MultipleResolution(final List<FeatureResolution<T>> resolutions, final T feature)
		{
			this.resolutions = resolutions;
			this.feature = feature;
		}

		@Override
		public void pushValue(final IFeaturedObject.Builder<?> builder)
		{
			resolutions.forEach(r -> r.pushValue(builder));
		}

		@Override
		public T feature()
		{
			return feature;
		}
	}
}

