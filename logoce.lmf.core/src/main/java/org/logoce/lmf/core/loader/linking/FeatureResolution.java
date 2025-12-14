package org.logoce.lmf.core.loader.linking;

import org.logoce.lmf.core.api.model.IFeaturedObject;

@FunctionalInterface
public interface FeatureResolution<T>
{
	void pushValue(IFeaturedObject.Builder<?> builder);

	default T feature()
	{
		return null;
	}
}

