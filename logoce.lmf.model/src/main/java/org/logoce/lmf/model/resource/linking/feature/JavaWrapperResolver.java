package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.resource.linking.FeatureLink;
import org.logoce.lmf.model.resource.linking.tree.LinkNode;

import java.util.Optional;

public class JavaWrapperResolver extends AttributeResolver<Object>
{
	public JavaWrapperResolver(final Attribute<Object, ?> attribute)
	{
		super(attribute);
		assert attribute.datatype() instanceof JavaWrapper;
	}

	@Override
	protected Optional<? extends FeatureLink> internalResolve(final LinkNode<?, ?> node, final String value)
	{
		return Optional.empty();
	}
}
