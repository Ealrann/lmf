package isotropy.lmf.core.resource.transform.feature;

import isotropy.lmf.core.model.IFeaturedObject;

public interface IFeatureResolution
{
	void pushValue(IFeaturedObject.Builder<?> builder);
}