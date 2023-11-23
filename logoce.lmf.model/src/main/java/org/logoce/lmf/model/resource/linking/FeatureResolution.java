package org.logoce.lmf.model.resource.linking;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Feature;

public interface FeatureResolution<T extends Feature<?, ?>>
{
	void pushValue(IFeaturedObject.Builder<?> builder);

	T feature();
}
