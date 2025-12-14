package org.logoce.lmf.core.feature;

public final class FeatureMap<T>
{
	private final T[] values;
	private final FeatureIndexFunction indexFunction;

	public FeatureMap(final T[] values, FeatureIndexFunction indexFunction)
	{
		this.values = values;
		this.indexFunction = indexFunction;
	}

	public T get(final int id)
	{
		return values[indexFunction.index(id)];
	}

	public static final class Builder<T>
	{
		private final T[] values;
		private final FeatureIndexFunction indexFunction;

		@SuppressWarnings("unchecked")
		public Builder(int featureCount, final FeatureIndexFunction indexFunction)
		{
			this.values = (T[]) new Object[featureCount];
			this.indexFunction = indexFunction;
		}

		public Builder<T> add(int featureID, T object)
		{
			values[indexFunction.index(featureID)] = object;
			return this;
		}

		public FeatureMap<T> build()
		{
			return new FeatureMap<>(values, indexFunction);
		}
	}
}
