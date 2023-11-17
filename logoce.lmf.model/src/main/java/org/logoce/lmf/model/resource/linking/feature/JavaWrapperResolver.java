package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.resource.linking.FeatureResolution;
import org.logoce.lmf.model.resource.linking.tree.LinkNodeInternal;

import java.util.Optional;

public class JavaWrapperResolver extends AttributeResolver<Object>
{
	public JavaWrapperResolver(final Attribute<Object, ?> attribute)
	{
		super(attribute);
		assert attribute.datatype() instanceof JavaWrapper;
	}

	@Override
	protected Optional<? extends FeatureResolution> internalResolve(final LinkNodeInternal<?, ?> node, final String value)
	{
		return Optional.empty();
	}
}
