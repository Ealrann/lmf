package logoce.lmf.model.resource.transform.word.resolver;

import logoce.lmf.model.lang.Attribute;
import logoce.lmf.model.lang.Enum;
import logoce.lmf.model.lang.Model;
import logoce.lmf.model.resource.transform.node.TreeBuilderNode;
import logoce.lmf.model.resource.transform.word.IFeatureResolution;

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
	protected Optional<IFeatureResolution> internalResolve(final TreeBuilderNode<?> node, final String value)
	{
		final var resolvedEnum = extractEnumLiteral(value, enumeration);
		return resolvedEnum.map(enumVal -> new AttributeResolution<>(feature, enumVal));
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
