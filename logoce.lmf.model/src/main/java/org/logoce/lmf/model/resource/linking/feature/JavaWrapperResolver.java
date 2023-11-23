package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.resource.linking.FeatureResolution;

import java.util.Optional;

public class JavaWrapperResolver extends AttributeResolver
{
	public JavaWrapperResolver(final Attribute<?, ?> attribute)
	{
		super(attribute);
		assert attribute.datatype() instanceof JavaWrapper;
	}

	@Override
	protected Optional<FeatureResolution<Attribute<?, ?>>> internalResolve(final String value)
	{
		return Optional.empty();
	}
}
