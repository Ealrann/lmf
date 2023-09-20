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
import org.logoce.notification.api.BooleanConsumer;
import org.logoce.notification.api.FloatConsumer;

import javax.lang.model.element.Modifier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

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

	public TypeName notificationCallbackType()
	{
		if (!isPrimitive())
		{
			final var consumerType = TypeParameter.of(Consumer.class);
			final var res = consumerType.nest(singleType.parametrizedWildcard());
			return res.parametrized();
		}
		else
		{
			final var attribute = (Attribute<?, ?>) feature;
			final var unit = (Unit<?>) attribute.datatype();
			return switch (unit.primitive())
			{
				case Boolean -> ClassName.get(BooleanConsumer.class);
				case Int -> ClassName.get(IntConsumer.class);
				case Long -> ClassName.get(LongConsumer.class);
				case Float -> ClassName.get(FloatConsumer.class);
				case Double -> ClassName.get(DoubleConsumer.class);
				case String -> throw new IllegalStateException();
			};
		}
	}

	private boolean isPrimitive()
	{
		if (feature instanceof Attribute<?, ?> attribute && attribute.datatype() instanceof Unit<?> unit)
		{
			return unit.primitive() != Primitive.String;
		}
		else
		{
			return false;
		}
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
				final var domain = javaWrapper.domain();
				final var name = javaWrapper.name();
				final var className = ClassName.get(domain, name);
				final var genricCount = GenUtils.genericCount(domain + "." + name);
				if (genricCount != 0)
				{
					if (parameters.isEmpty())
					{
						return new PartialFeatureResolution(TypeParameter.of(className, genricCount), true);
					}
					else
					{
						final var params = TypeResolutionUtil.toParameters(parameters);
						return new PartialFeatureResolution(TypeParameter.of(className, params), true);
					}
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
