package org.logoce.lmf.core.loader.feature;

import org.logoce.lmf.core.api.model.DynamicModelPackage;
import org.logoce.lmf.core.api.model.IJavaWrapperConverter;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.JavaWrapper;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.loader.api.loader.linking.FeatureResolution;

import java.util.Optional;

public final class JavaWrapperResolver<Y> extends AttributeResolver
{
	private final Attribute<Y, ?, ?, ?> typedFeature;
	private final JavaWrapper<Y> wrapper;

	@SuppressWarnings("unchecked")
	public JavaWrapperResolver(final Attribute<?, ?, ?, ?> attribute)
	{
		super(attribute);
		assert attribute.datatype() instanceof JavaWrapper;
		typedFeature = (Attribute<Y, ?, ?, ?>) attribute;
		wrapper = (JavaWrapper<Y>) attribute.datatype();
	}

	@Override
	protected Optional<FeatureResolution<Attribute<?, ?, ?, ?>>> internalResolve(final String value)
	{
		final var converter = resolveConverter(wrapper);
		if (converter.isEmpty())
		{
			if (isDynamicWrapper(wrapper))
			{
				return buildDynamicResolution(value);
			}
			return Optional.empty();
		}

		try
		{
			final var parsedValue = converter.get().create(value);
			return Optional.of(new AttributeResolution<>(typedFeature, parsedValue));
		}
		catch (final RuntimeException e)
		{
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private Optional<FeatureResolution<Attribute<?, ?, ?, ?>>> buildDynamicResolution(final String value)
	{
		final var dynamicFeature = (Attribute<String, ?, ?, ?>) typedFeature;
		return Optional.of(new AttributeResolution<>(dynamicFeature, value));
	}

	private static boolean isDynamicWrapper(final JavaWrapper<?> wrapper)
	{
		if (!(wrapper.lmContainer() instanceof MetaModel metaModel)) return false;
		return metaModel.lmPackage() instanceof DynamicModelPackage;
	}

	private static <T> Optional<IJavaWrapperConverter<T>> resolveConverter(final JavaWrapper<T> wrapper)
	{
		if (!(wrapper.lmContainer() instanceof MetaModel metaModel)) return Optional.empty();

		final var pkg = metaModel.lmPackage();
		if (pkg == null) return Optional.empty();

		return pkg.resolveJavaWrapperConverter(wrapper);
	}
}
