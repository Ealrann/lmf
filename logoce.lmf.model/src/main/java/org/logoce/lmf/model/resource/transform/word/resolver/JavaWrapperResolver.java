package org.logoce.lmf.model.resource.transform.word.resolver;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.resource.transform.node.TreeBuilderNode;
import org.logoce.lmf.model.resource.transform.word.IFeatureResolution;

import java.util.Optional;

public class JavaWrapperResolver extends AttributeResolver<Object>
{
	private final JavaWrapper<?> javaWrapper;

	public JavaWrapperResolver(final Attribute<Object, ?> attribute)
	{
		super(attribute);
		javaWrapper = (JavaWrapper<?>) attribute.datatype();
	}

	@Override
	protected Optional<? extends IFeatureResolution> internalResolve(final TreeBuilderNode<?> node, final String value)
	{
		return Optional.empty();
	}
}
