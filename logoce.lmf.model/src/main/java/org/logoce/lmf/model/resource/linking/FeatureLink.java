package org.logoce.lmf.model.resource.linking;

import org.logoce.lmf.model.api.model.IFeaturedObject;

public interface FeatureLink
{
	void pushValue(IFeaturedObject.Builder<?> builder);
}
