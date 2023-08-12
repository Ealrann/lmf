package isotropy.lmf.core.resource.transform.feature.resolver;

import isotropy.lmf.core.resource.transform.feature.IFeatureResolution;
import isotropy.lmf.core.resource.transform.util.BuilderNode;

import java.util.Optional;

public interface IChildResolver<T> extends IFeatureResolver<T>
{
	Optional<? extends IFeatureResolution> resolve(BuilderNode child);
}