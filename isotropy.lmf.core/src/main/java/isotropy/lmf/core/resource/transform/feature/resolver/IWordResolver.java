package isotropy.lmf.core.resource.transform.feature.resolver;

import isotropy.lmf.core.resource.transform.feature.IFeatureResolution;
import isotropy.lmf.core.resource.transform.node.BuilderNode;
import isotropy.lmf.core.resource.util.Tree;

import java.util.Optional;

public interface IWordResolver<T> extends IFeatureResolver<T>
{
	Optional<? extends IFeatureResolution> resolve(Tree<BuilderNode<?>> node, String value);

	boolean isBooleanAttribute();
}