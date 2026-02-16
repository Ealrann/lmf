package org.logoce.lmf.core.loader.feature;

import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.loader.api.loader.linking.InvalidEnumLiteralException;
import org.logoce.lmf.core.loader.api.loader.linking.FeatureResolution;

import java.util.Optional;

public final class EnumResolver<Y> extends AttributeResolver
{
	private final Enum<Y> enumeration;

	@SuppressWarnings("unchecked")
	public EnumResolver(final Attribute<?, ?, ?, ?> attribute)
	{
		super(attribute);
		enumeration = (Enum<Y>) attribute.datatype();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Optional<FeatureResolution<Attribute<?, ?, ?, ?>>> internalResolve(final String value)
	{
		final var mm = (MetaModel) enumeration.lmContainer();
		if (mm == null)
		{
			return Optional.empty();
		}

		final var pkg = mm.lmPackage();
		if (pkg == null)
		{
			return Optional.empty();
		}

		final var resolved = pkg.resolveEnumLiteral(enumeration, capitalizeFirstLetter(value));
		if (resolved.isEmpty())
		{
			throw new InvalidEnumLiteralException(feature.name(), value, enumeration.literals());
		}
		return resolved.map(enumVal -> new AttributeResolution<>((Attribute<Y, ?, ?, ?>) feature, enumVal));
	}

	private static String capitalizeFirstLetter(final String str)
	{
		if (str == null || str.isEmpty())
		{
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
}
