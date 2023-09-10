package isotropy.lmf.core.resource.transform.word;

import isotropy.lmf.core.api.model.IFeaturedObject;

public interface IFeatureResolution
{
	void pushValue(IFeaturedObject.Builder<?> builder);
}
