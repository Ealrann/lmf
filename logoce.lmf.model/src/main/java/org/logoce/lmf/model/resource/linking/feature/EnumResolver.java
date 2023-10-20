package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.resource.linking.FeatureLink;
import org.logoce.lmf.model.resource.linking.tree.ResolvedNode;

import java.util.Optional;

public final class EnumResolver<T> extends AttributeResolver<T>
{
	private final Enum<T> enumeration;

	public EnumResolver(final Attribute<T, ?> attribute)
	{
		super(attribute);
		enumeration = (Enum<T>) attribute.datatype();
	}

	@Override
	protected Optional<FeatureLink> internalResolve(final ResolvedNode<?, ?> node, final String value)
	{
		final var resolvedEnum = extractEnumLiteral(value, enumeration);
		return resolvedEnum.map(enumVal -> new AttributeLink<>(feature, enumVal));
	}

	private static <T> Optional<T> extractEnumLiteral(final String value, final Enum<T> _enum)
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
