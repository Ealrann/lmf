package org.logoce.lmf.model.feature;

import org.logoce.lmf.model.api.feature.RawFeature;

import java.util.ArrayList;
import java.util.List;

public final class FeatureMap<T>
{
	private final List<FeatureTuple<T, ?>> features;

	public FeatureMap(final List<FeatureTuple<T, ?>> features)
	{
		this.features = List.copyOf(features);
	}

	public T get(final RawFeature<?, ?> feature)
	{
		return getTuple(feature).value;
	}

	@SuppressWarnings("unchecked")
	public <F> FeatureTuple<T, F> getTuple(final RawFeature<?, F> feature)
	{
		for (int i = 0; i < features.size(); i++)
		{
			final var entry = features.get(i);
			if (entry.feature == feature)
			{
				return (FeatureTuple<T, F>) entry;
			}
		}
		throw new IllegalArgumentException("This Feature doesn't belong to this FeatureMap");
	}

	public record FeatureTuple<T, F>(RawFeature<?, F> feature, T value)
	{}

	public static final class Builder<T>
	{
		private final List<FeatureTuple<T, ?>> features = new ArrayList<>();

		public Builder()
		{
		}

		public FeatureMap<T> build()
		{
			return new FeatureMap<>(List.copyOf(features));
		}

		public Builder<T> add(final RawFeature<?, ?> feature, final T value)
		{
			features.add(new FeatureTuple<>(feature, value));
			return this;
		}
	}
}
