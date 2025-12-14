package org.logoce.lmf.core.loader.linking.feature;

import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.JavaWrapper;
import org.logoce.lmf.core.loader.linking.FeatureResolution;

import java.util.Optional;

public final class JavaWrapperResolver extends AttributeResolver
{
	public JavaWrapperResolver(final Attribute<?, ?, ?, ?> attribute)
	{
		super(attribute);
		assert attribute.datatype() instanceof JavaWrapper;
	}

	@Override
	protected Optional<FeatureResolution<Attribute<?, ?, ?, ?>>> internalResolve(final String value)
	{
		return Optional.empty();
	}
}
