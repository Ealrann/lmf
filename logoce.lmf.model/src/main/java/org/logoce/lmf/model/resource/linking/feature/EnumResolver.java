package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.resource.linking.FeatureResolution;

import java.util.Optional;

public final class EnumResolver<Y> extends AttributeResolver
{
	private final Enum<Y> enumeration;

	public EnumResolver(final Attribute<Y, ?> attribute)
	{
		super(attribute);
		enumeration = (Enum<Y>) attribute.datatype();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Optional<FeatureResolution<Attribute<?, ?>>> internalResolve(final String value)
	{
		final var resolvedEnum = extractEnumLiteral(value, enumeration);
		return resolvedEnum.map(enumVal -> new AttributeResolution<>((Attribute<Y, ?>) feature, enumVal));
	}

	private Optional<Y> extractEnumLiteral(final String value, final Enum<Y> _enum)
	{
		final var lPackage = ((Model) _enum.lmContainer()).lPackage();
		final var resolvedEnum = lPackage.resolveEnumLiteral(_enum, capitalizeFirstLetter(value));
		return resolvedEnum;
	}

	public static String capitalizeFirstLetter(String str)
	{
		if (str == null || str.isEmpty())
		{
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
}
