package isotropy.lmf.core.resource.transform.word.resolver;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.JavaWrapper;
import isotropy.lmf.core.resource.transform.node.TreeBuilderNode;
import isotropy.lmf.core.resource.transform.word.IFeatureResolution;

import java.util.Optional;

public class JavaWrapperResolver extends AttributeResolver<Object>
{
	private final JavaWrapper javaWrapper;

	public JavaWrapperResolver(final Attribute<Object, ?> attribute)
	{
		super(attribute);
		javaWrapper = (JavaWrapper) attribute.datatype();
	}

	@Override
	protected Optional<? extends IFeatureResolution> internalResolve(final TreeBuilderNode<?> node, final String value)
	{
		return Optional.empty();
	}
}
