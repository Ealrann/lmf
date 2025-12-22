package org.logoce.lmf.core.loader.feature;

import org.logoce.lmf.core.api.model.IJavaWrapperConverter;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.JavaWrapper;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.loader.api.loader.linking.FeatureResolution;

import java.util.Optional;

public final class JavaWrapperResolver extends AttributeResolver
{
	private final JavaWrapper<?> wrapper;

	public JavaWrapperResolver(final Attribute<?, ?, ?, ?> attribute)
	{
		super(attribute);
		assert attribute.datatype() instanceof JavaWrapper;
		wrapper = (JavaWrapper<?>) attribute.datatype();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	protected Optional<FeatureResolution<Attribute<?, ?, ?, ?>>> internalResolve(final String value)
	{
		final var converter = resolveConverter(wrapper);
		if (converter.isEmpty()) return Optional.empty();

		try
		{
			final var parsedValue = ((IJavaWrapperConverter<Object>) converter.get()).create(value);
			return Optional.of(new AttributeResolution<>((Attribute<Object, ?, ?, ?>) feature, parsedValue));
		}
		catch (final RuntimeException e)
		{
			return Optional.empty();
		}
	}

	private static Optional<IJavaWrapperConverter<?>> resolveConverter(final JavaWrapper<?> wrapper)
	{
		if (!(wrapper.lmContainer() instanceof MetaModel metaModel)) return Optional.empty();

		final var pkg = metaModel.lmPackage();
		if (pkg == null) return Optional.empty();

		return (Optional) pkg.resolveJavaWrapperConverter((JavaWrapper) wrapper);
	}
}
