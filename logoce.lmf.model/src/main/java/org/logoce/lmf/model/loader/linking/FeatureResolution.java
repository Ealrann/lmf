package org.logoce.lmf.model.loader.linking;

import org.logoce.lmf.model.api.model.IFeaturedObject;

@FunctionalInterface
public interface FeatureResolution<T>
{
	void pushValue(IFeaturedObject.Builder<?> builder);

	default T feature()
	{
		return null;
	}
}

