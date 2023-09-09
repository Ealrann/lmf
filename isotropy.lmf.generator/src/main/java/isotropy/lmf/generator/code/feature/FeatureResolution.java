package isotropy.lmf.generator.code.feature;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.generator.util.ConstantTypes;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.TypeParameter;
import isotropy.lmf.generator.util.TypeResolutionUtil;

import javax.lang.model.element.Modifier;

public record FeatureResolution(Feature<?, ?> feature,
								TypeParameter singleType,
								TypeParameter effectiveType,
								boolean hasGeneric)
{
	public String name()
	{
		return feature.name();
	}

	public TypeParameter implementationType()
	{
		if (feature instanceof Relation<?, ?> relation && relation.lazy())
		{
			final var suppliedType = singleType.nestIn(ConstantTypes.SUPPLIER);
			return feature.many() ? suppliedType.nestIn(ConstantTypes.LIST) : suppliedType;
		}
		else
		{
			return effectiveType;
		}
	}

	public TypeName builderType()
	{
		final var relation = feature instanceof Relation<?, ?>;
		final var many = feature.many();
		if (!relation) return effectiveType().parametrized();
		else if (many) return singleType.nestIn(ConstantTypes.SUPPLIER).nestIn(ConstantTypes.LIST).parametrized();
		else return singleType.nestIn(ConstantTypes.SUPPLIER).parametrized();
	}

	public ParameterSpec parameterSpec()
	{
		final var name = MethodUtil.validateParameterName(name());
		final var type = implementationType().parametrized();
		return ParameterSpec.builder(type, name).addModifiers(Modifier.FINAL).build();
	}

	public ParameterSpec builderParameterSpec()
	{
		final var relation = feature instanceof Relation<?, ?>;
		final var name = MethodUtil.builderSingleParameterName(this);
		final var type = relation ? singleType.nestIn(ConstantTypes.SUPPLIER) : singleType;
		final var paramType = type.parametrized();
		return ParameterSpec.builder(paramType, name).build();
	}

	public static FeatureResolution from(Feature<?, ?> feature)
	{
		final var partialResolution = resolveType(feature);
		final var effectiveType = encapsulateEffectiveType(feature, partialResolution.singleType());

		return new FeatureResolution(feature,
									 partialResolution.singleType(),
									 effectiveType,
									 partialResolution.containsGeneric());
	}

	private static TypeParameter encapsulateEffectiveType(final Feature<?, ?> feature, TypeParameter singleType)
	{
		if (feature.many())
		{
			return singleType.nestIn(ConstantTypes.LIST);
		}
		else
		{
			return singleType;
		}
	}

	private static PartialFeatureResolution resolveType(final Feature<?, ?> feature)
	{
		if (feature instanceof Attribute<?, ?> attribute)
		{
			final var datatype = attribute.datatype();
			if (datatype instanceof Unit<?> unit)
			{
				final var primitiveType = GenUtils.resolvePrimitiveClass(unit.primitive());
				final var typeName = TypeName.get(primitiveType);
				return new PartialFeatureResolution(TypeParameter.ofPrimitive(typeName), false);
			}
			else if (datatype instanceof Enum<?> enumeration)
			{
				final var model = (Model) enumeration.lmContainer();
				final var className = ClassName.get(model.domain(), enumeration.name());
				return new PartialFeatureResolution(TypeParameter.of(className), false);
			}
			else
			{
				final var javaWrapper = (JavaWrapper<?>) datatype;
				final var parameters = attribute.parameters();
				final var className = ClassName.get(javaWrapper.domain(), javaWrapper.name());
				if (!parameters.isEmpty())
				{
					final var params = TypeResolutionUtil.toParameters(parameters);
					return new PartialFeatureResolution(TypeParameter.of(className, params), true);
				}
				else
				{
					return new PartialFeatureResolution(TypeParameter.of(className), false);
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
				final var parameters = reference.parameters();
				final var containsGeneric = parameters.stream().anyMatch(Generic.class::isInstance);
				return new PartialFeatureResolution(TypeResolutionUtil.parametrizedType(group, parameters),
													containsGeneric);
			}
			else
			{
				final var generic = (Generic<?>) concept;
				final var className = ClassName.get("", generic.name());
				return new PartialFeatureResolution(TypeParameter.of(className), true);
			}
		}
	}

	private record PartialFeatureResolution(TypeParameter singleType, boolean containsGeneric) {}
}
