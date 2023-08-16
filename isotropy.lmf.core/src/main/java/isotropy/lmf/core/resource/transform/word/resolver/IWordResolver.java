package isotropy.lmf.core.resource.transform.word.resolver;

import isotropy.lmf.core.resource.transform.node.TreeBuilderNode;
import isotropy.lmf.core.resource.transform.word.IFeatureResolution;

import java.util.Optional;

public interface IWordResolver<T>
{
	boolean match(String featureName);
	Optional<? extends IFeatureResolution> resolve(TreeBuilderNode<?> node, String value);

	boolean isBooleanAttribute();
}
