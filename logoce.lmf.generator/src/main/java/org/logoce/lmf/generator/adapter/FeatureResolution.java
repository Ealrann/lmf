package org.logoce.lmf.generator.adapter;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import org.logoce.lmf.adapter.api.Adapter;
import org.logoce.lmf.core.lang.*;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.extender.api.ModelExtender;
import org.logoce.lmf.generator.code.feature.MethodUtil;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.generator.util.TypeResolutionUtil;
import org.logoce.lmf.notification.api.BooleanConsumer;
import org.logoce.lmf.notification.api.FloatConsumer;

import javax.lang.model.element.Modifier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

@ModelExtender(scope = Feature.class)
@Adapter
public final class FeatureResolution implements IAdapter
{
	public final Feature<?, ?, ?, ?> feature;
	public final TypeParameter singleType;
	public final TypeParameter effectiveType;
	public final TypeParameter rawSingleType;
	public final TypeParameter rawEffectiveType;
	public final boolean hasGeneric;

	private FeatureResolution(final Feature<?, ?, ?, ?> feature)
	{
		final var partialResolution = resolveType(feature);
		final var effectiveType = encapsulateEffectiveType(feature, partialResolution.singleType());
		final var rawEffectiveType = encapsulateEffectiveType(feature, partialResolution.rawSingleType());

		this.feature = feature;
		this.singleType = partialResolution.singleType();
		this.effectiveType = effectiveType;
		this.rawSingleType = partialResolution.rawSingleType();
		this.rawEffectiveType = rawEffectiveType;
		this.hasGeneric = partialResolution.containsGeneric();
	}

	public Feature<?, ?, ?, ?> feature()
	{
		return feature;
	}

	public TypeParameter singleType()
	{
		return singleType;
	}

	public TypeParameter effectiveType()
	{
		return effectiveType;
	}

	public TypeParameter singleTypeFor(final Group<?> owner)
	{
		return resolveSingleTypeForOwner(owner);
	}

	public TypeParameter effectiveTypeFor(final Group<?> owner)
	{
		return encapsulateEffectiveType(feature, resolveSingleTypeForOwner(owner));
	}

	public TypeParameter rawSingleType()
	{
		return rawSingleType;
	}

	public TypeParameter rawEffectiveType()
	{
		return rawEffectiveType;
	}

	public TypeParameter rawSingleTypeFor(final Group<?> owner)
	{
		if (feature instanceof Attribute<?, ?, ?, ?> attribute && attribute.datatype() instanceof Generic<?> generic)
		{
			final var bound = TypeResolutionUtil.resolveGenericBinding(generic, owner);
			if (bound != null)
			{
				return bound;
			}
		}

		if (feature instanceof Relation<?, ?, ?, ?> relation && relation.concept() instanceof Generic<?> generic)
		{
			final var extension = generic.extension();
			if (extension != null && extension.type() != null)
			{
				return TypeResolutionUtil.resolveSimpleType(extension.type());
			}
		}

		return rawSingleType;
	}

	public TypeParameter rawEffectiveTypeFor(final Group<?> owner)
	{
		return encapsulateEffectiveType(feature, rawSingleTypeFor(owner));
	}

	public boolean hasGeneric()
	{
		return hasGeneric;
	}

	public boolean requiresOwnerSpecialization(final Group<?> owner)
	{
		if (owner == feature.lmContainer())
		{
			return false;
		}

		if (feature instanceof Attribute<?, ?, ?, ?> attribute && attribute.datatype() instanceof Generic<?> generic)
		{
			return TypeResolutionUtil.resolveGenericBinding(generic, owner) != null;
		}

		return false;
	}

	public String name()
	{
		return feature.name();
	}

	public TypeParameter implementationType()
	{
		if (feature instanceof Relation<?, ?, ?, ?> relation && relation.lazy())
		{
			final var suppliedType = singleType.nestIn(ConstantTypes.SUPPLIER);
			return feature.many() ? suppliedType.nestIn(ConstantTypes.LIST) : suppliedType;
		}
		else
		{
			return effectiveType;
		}
	}

	public TypeParameter implementationTypeFor(final Group<?> owner)
	{
		if (feature instanceof Relation<?, ?, ?, ?> relation && relation.lazy())
		{
			final var suppliedType = resolveSingleTypeForOwner(owner).nestIn(ConstantTypes.SUPPLIER);
			return feature.many() ? suppliedType.nestIn(ConstantTypes.LIST) : suppliedType;
		}
		else
		{
			return effectiveTypeFor(owner);
		}
	}

	public TypeName builderType()
	{
		return builderTypeFor((Group<?>) feature.lmContainer());
	}

	public TypeName builderTypeFor(final Group<?> owner)
	{
		final var relation = feature instanceof Relation<?, ?, ?, ?>;
		final var many = feature.many();
		final var single = singleTypeFor(owner);
		final var effective = effectiveTypeFor(owner);
		if (!relation) return effective.parametrized();
		else if (many) return single.nestIn(ConstantTypes.SUPPLIER).nestIn(ConstantTypes.LIST).parametrized();
		else return single.nestIn(ConstantTypes.SUPPLIER).parametrized();
	}

	public ParameterSpec parameterSpec()
	{
		return parameterSpec((Group<?>) feature.lmContainer());
	}

	public ParameterSpec parameterSpec(final Group<?> owner)
	{
		final var name = MethodUtil.validateParameterName(name());
		final var type = implementationTypeFor(owner).parametrized();
		return ParameterSpec.builder(type, name).addModifiers(Modifier.FINAL).build();
	}

	public ParameterSpec builderParameterSpec()
	{
		return builderParameterSpec((Group<?>) feature.lmContainer());
	}

	public ParameterSpec builderParameterSpec(final Group<?> owner)
	{
		final var relation = feature instanceof Relation<?, ?, ?, ?>;
		final var name = MethodUtil.builderSingleParameterName(this);
		final var resolvedSingleType = resolveSingleTypeForOwner(owner);
		final var type = relation ? resolvedSingleType.nestIn(ConstantTypes.SUPPLIER) : resolvedSingleType;
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
			final var attribute = (Attribute<?, ?, ?, ?>) feature;
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
		if (feature instanceof Attribute<?, ?, ?, ?> attribute && attribute.datatype() instanceof Unit<?> unit)
		{
			return unit.primitive() != Primitive.String;
		}
		else
		{
			return false;
		}
	}

	private static TypeParameter encapsulateEffectiveType(final Feature<?, ?, ?, ?> feature, TypeParameter singleType)
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

	private TypeParameter resolveSingleTypeForOwner(final Group<?> owner)
	{
		if (feature instanceof Attribute<?, ?, ?, ?> attribute && attribute.datatype() instanceof Generic<?> generic)
		{
			final var bound = TypeResolutionUtil.resolveGenericBinding(generic, owner);
			if (bound != null)
			{
				return bound;
			}
		}
		return singleType;
	}

	private static PartialFeatureResolution resolveType(final Feature<?, ?, ?, ?> feature)
	{
		if (feature instanceof Attribute<?, ?, ?, ?> attribute)
			{
				final var datatype = attribute.datatype();
				switch (datatype)
				{
					case Unit<?> unit ->
					{
						final var primitiveType = GenUtils.resolvePrimitiveClass(unit.primitive());
						final var typeName = TypeName.get(primitiveType);
						final var typeParameter = TypeParameter.ofPrimitive(typeName);
						return new PartialFeatureResolution(typeParameter, typeParameter, false);
					}
					case Enum<?> enumeration ->
					{
						final var model = (MetaModel) enumeration.lmContainer();
						final var className = ClassName.get(org.logoce.lmf.generator.util.TargetPathUtil.packageName(model), enumeration.name());
						final var typeParameter = TypeParameter.of(className);
						return new PartialFeatureResolution(typeParameter, typeParameter, false);
					}
					case Generic<?> generic ->
					{
						final var extension = generic.extension();
						if (extension != null && extension.type() instanceof Group<?> group)
						{
							throw new IllegalArgumentException(
									"Generic %s bound to group %s cannot be used as attribute datatype".formatted(
											generic.name(),
											group.name()));
						}

						final var resolution = TypeResolutionUtil.resolveGenericDatatype(generic);
						return new PartialFeatureResolution(resolution.resolvedType(),
															resolution.rawType(),
															resolution.containsGeneric());
					}
					case null, default ->
					{
						final var javaWrapper = (JavaWrapper<?>) datatype;
						final var parameters = attribute.parameters();
						final var qualifiedName = javaWrapper.qualifiedClassName();
						final var className = ClassName.bestGuess(qualifiedName);
						final var genericCount = GenUtils.genericCount(qualifiedName);
						if (genericCount != 0)
						{
							if (parameters.isEmpty())
							{
								final var typeParameter = TypeParameter.of(className, genericCount);
								return new PartialFeatureResolution(typeParameter, typeParameter, true);
							}
							else
							{
								final var params = parameters.stream()
															 .map(org.logoce.lmf.generator.util.GenericParameter::resolveParameterType)
															 .toList();
								final var typeParameter = TypeParameter.of(className, params);
								return new PartialFeatureResolution(typeParameter, typeParameter, true);
							}
						}
						else
						{
							final var typeParameter = TypeParameter.of(className);
							return new PartialFeatureResolution(typeParameter, typeParameter, false);
						}
					}
				}
			}
			else
			{
				final var relation = (Relation<?, ?, ?, ?>) feature;
				final var concept = relation.concept();
				final var parameters = relation.parameters();
				if (concept instanceof Group<?> group)
				{
					final var containsGeneric = parameters.stream()
														   .map(GenericParameter::type)
														   .anyMatch(Generic.class::isInstance);
					final var typeParameter = TypeResolutionUtil.parametrizedType(group, parameters);
					return new PartialFeatureResolution(typeParameter,
														typeParameter,
														containsGeneric);
				}
				else
				{
					final var generic = (Generic<?>) concept;
					final var className = ClassName.get("", generic.name());
					final var typeParameter = TypeParameter.of(className);
					return new PartialFeatureResolution(typeParameter, typeParameter, true);
				}
			}
		}

	private record PartialFeatureResolution(TypeParameter singleType,
											TypeParameter rawSingleType,
											boolean containsGeneric) {}
}
