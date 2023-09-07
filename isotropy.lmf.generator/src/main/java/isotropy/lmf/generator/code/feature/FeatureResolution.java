package isotropy.lmf.generator.code.feature;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.generator.util.ConstantTypes;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.TypeParameter;
import isotropy.lmf.generator.util.TypeResolutionUtil;

public record FeatureResolution(Feature<?, ?> feature, TypeParameter singleType, TypeParameter effectiveType)
{
	public String name()
	{
		return feature.name();
	}

	public static FeatureResolution from(Feature<?, ?> feature)
	{
		final var singleTypeParameter = resolveType(feature);
		final var effectiveType = encapsulateEffectiveType(feature, singleTypeParameter);

		return new FeatureResolution(feature, singleTypeParameter, effectiveType);
	}

	private static TypeParameter encapsulateEffectiveType(final Feature<?, ?> feature, TypeParameter singleType)
	{
		if (feature.many())
		{
			return singleType.nestIn(ConstantTypes.LIST_CLASS_NAME);
		}
		else
		{
			return singleType;
		}
	}

	private static TypeParameter resolveType(final Feature<?, ?> feature)
	{
		if (feature instanceof Attribute<?, ?> attribute)
		{
			final var datatype = attribute.datatype();
			if (datatype instanceof Unit<?> unit)
			{
				final var primitiveType = GenUtils.resolvePrimitiveClass(unit.primitive());
				final var typeName = TypeName.get(primitiveType);
				return TypeParameter.of(typeName);
			}
			else if (datatype instanceof Enum<?> enumeration)
			{
				final var model = (Model) enumeration.lmContainer();
				final var className = ClassName.get(model.domain(), enumeration.name());
				return TypeParameter.of(className);
			}
			else
			{
				final var javaWrapper = (JavaWrapper<?>) datatype;
				final var parameters = attribute.parameters();
				final var className = ClassName.get(javaWrapper.domain(), javaWrapper.name());
				if (!parameters.isEmpty())
				{
					final var params = TypeResolutionUtil.toParameters(parameters);
					return TypeParameter.of(className, params);
				}
				else
				{
					return TypeParameter.of(className);
				}
			}
		}
		else
		{
			final var relation = (Relation<?, ?>) feature;
			final var reference = relation.reference();
			final var concept = reference.group();
			if (concept instanceof Group<?> group)
			{
				return TypeResolutionUtil.parametrizedType(group, reference.parameters());
			}
			else
			{
				final var generic = (Generic<?>) concept;
				final var className = ClassName.get("", generic.name());
				return TypeParameter.of(className);
			}
		}
	}
}
