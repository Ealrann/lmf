package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.resource.linking.FeatureLink;
import org.logoce.lmf.model.resource.linking.tree.ResolvedNode;

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
	protected Optional<? extends FeatureLink> internalResolve(final ResolvedNode<?, ?> node, final String value)
	{
		return Optional.empty();
	}
}
