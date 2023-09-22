package logoce.lmf.model.resource.transform.word;

import logoce.lmf.model.api.model.IFeaturedObject;

public interface IFeatureResolution
{
	void pushValue(IFeaturedObject.Builder<?> builder);
}
