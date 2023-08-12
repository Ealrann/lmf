package isotropy.lmf.core.model;

import isotropy.lmf.core.lang.Feature;

import java.util.ArrayList;
import java.util.List;

public final class FeatureMap<T>
{
	private final List<FeatureTuple<T, ?>> features;

	public FeatureMap(final List<FeatureTuple<T, ?>> features)
	{
		this.features = List.copyOf(features);
	}

	public T get(final Feature<?, ?> feature)
	{
		return getTuple(feature).value;
	}

	@SuppressWarnings("unchecked")
	public <F> FeatureTuple<T, F> getTuple(final Feature<?, F> feature)
	{
		for (int i = 0; i < features.size(); i++)
		{
			final var entry = features.get(i);
			if (entry.feature == feature)
			{
				return (FeatureTuple<T, F>) entry;
			}
		}
		throw new IllegalArgumentException("Feature " + feature.name() + " doesn't belong to this FeatureMap");
	}

	public static final class FeatureTuple<T, F>
	{
		public final Feature<?, F> feature;
		public final T value;

		public FeatureTuple(Feature<?, F> feature, T value)
		{
			this.feature = feature;
			this.value = value;
		}
	}

	public static final class Builder<T>
	{
		private final List<FeatureTuple<T, ?>> features = new ArrayList<>();

		public FeatureMap<T> build()
		{
			return new FeatureMap<>(List.copyOf(features));
		}

		public Builder<T> add(final Feature<?, ?> feature, final T value)
		{
			features.add(new FeatureTuple<>(feature, value));
			return this;
		}
	}
}
