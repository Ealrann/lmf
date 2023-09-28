package org.logoce.lmf.model.resource.transform.word;

import org.logoce.lmf.model.api.model.IFeaturedObject;

public interface IFeatureResolution
{
	void pushValue(IFeaturedObject.Builder<?> builder);
}
