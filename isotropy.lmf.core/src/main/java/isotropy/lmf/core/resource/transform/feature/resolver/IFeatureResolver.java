package isotropy.lmf.core.resource.transform.feature.resolver;

public interface IFeatureResolver<T>
{
	boolean match(String featureName);
}