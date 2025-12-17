package org.logoce.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.lang.*;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.core.lang.GenericParameter;

import java.util.List;

public class TypeResolutionUtil
{
	public static TypeParameter resolveInclude(final Include<?> refInclude, final Group<?> group)
	{
		final var params = refInclude.parameters()
									 .stream()
									 .map(org.logoce.lmf.generator.util.GenericParameter::resolveParameterType)
									 .toList();
		final var refIncludeGroup = refInclude.group();
		final var model = (MetaModel) refIncludeGroup.lmContainer();
		final var className = ClassName.get(TargetPathUtil.packageName(model), refIncludeGroup.name());
		return TypeParameter.of(className, params);
	}

	public static TypeParameter resolveNoInclude(final Group<?> group)
	{
		if (group.name().equals("LMObject"))
		{
			final var res = ClassName.get(IFeaturedObject.class);
			return TypeParameter.of(res);
		}
		else
		{
			return TypeParameter.of(ConstantTypes.LM_OBJECT);
		}
	}

	public static TypeParameter parametrizedType(Group<?> group, List<? extends GenericParameter> parameters)
	{
		final var model = (MetaModel) group.lmContainer();
		final var className = ClassName.get(TargetPathUtil.packageName(model), group.name());
		if (!parameters.isEmpty())
		{
			final var params = parameters.stream()
										 .map(org.logoce.lmf.generator.util.GenericParameter::resolveParameterType)
										 .toList();
			return TypeParameter.of(className, params);
		}
		else if (!group.generics().isEmpty())
		{
			final var params = group.generics().stream().map(g -> ClassName.get("", "?")).toList();
			return TypeParameter.of(className, params);
		}
		else
		{
			return TypeParameter.of(className);
		}
	}

	/**
	 * Resolve a simple Type (Group / Datatype / Enum / Unit / JavaWrapper) into a TypeParameter
	 * for use in operation signatures and other non-feature contexts.
	 */
	public static TypeParameter resolveSimpleType(final Type<?> type)
	{
		switch (type)
		{
			case null -> throw new IllegalArgumentException("type cannot be null");
			case Group<?> group ->
			{
				return parametrizedType(group, List.of());
			}
			case Enum<?> enumeration ->
			{
				final var model = (MetaModel) enumeration.lmContainer();
				final var className = ClassName.get(TargetPathUtil.packageName(model), enumeration.name());
				return TypeParameter.of(className);
			}
			case Unit<?> unit ->
			{
				final var primitiveClass = GenUtils.resolvePrimitiveClass(unit.primitive());
				final var typeName = TypeName.get(primitiveClass);
				return TypeParameter.ofPrimitive(typeName);
			}
			case JavaWrapper<?> wrapper ->
			{
				final var qualifiedName = wrapper.qualifiedClassName();
				final var className = ClassName.bestGuess(qualifiedName);
				final var genericCount = GenUtils.genericCount(qualifiedName);
				if (genericCount != 0)
				{
					return TypeParameter.of(className, genericCount);
				}
				else
				{
					return TypeParameter.of(className);
				}
			}
			case Generic<?> generic ->
			{
				final var typeName = TypeVariableName.get(generic.name());
				return new TypeParameter.SimpleType(typeName);
			}
			default -> throw new IllegalStateException("Unsupported type kind: " + type.getClass().getName());
		}

	}

	public static GenericResolution resolveGenericDatatype(final Generic<?> generic)
	{
		final var extension = generic.extension();
		final var hasExtensionType = extension != null && extension.type() != null;
		final var parameterTypes = hasExtensionType
								   ? extension.parameters()
											   .stream()
											   .map(org.logoce.lmf.generator.util.GenericParameter::resolveParameterType)
											   .toList()
								   : List.<TypeName>of();

		final TypeParameter resolvedType;
		final TypeParameter rawType;

		if (hasExtensionType)
		{
			final var baseType = resolveSimpleType(extension.type());
			resolvedType = parameterTypes.isEmpty() ? baseType : parameterize(baseType, parameterTypes);

			if (baseType instanceof TypeParameter.SimpleTypeParameter simple)
			{
				rawType = TypeParameter.of(simple.raw(), simple.parameters());
			}
			else if (baseType instanceof TypeParameter.CombinedTypeParameter combined)
			{
				rawType = TypeParameter.of(combined.raw(), combined.parameters());
			}
			else
			{
				rawType = resolvedType;
			}
		}
		else
		{
			resolvedType = resolveSimpleType(generic);
			rawType = TypeParameter.of(ClassName.get(Object.class));
		}

		return new GenericResolution(resolvedType, rawType, true);
	}

	public static String resolveTypeHolder(final Type<?> type)
	{
		return switch (type)
		{
			case Group<?> _ -> "Groups";
			case Enum<?> _ -> "Enums";
			case Unit<?> _ -> "Units";
			case JavaWrapper<?> _ -> "JavaWrappers";
			default -> null;
		};
	}

	public static String resolveConceptHolder(final Concept<?> concept)
	{
		return switch (concept)
		{
			case Group<?> _ -> "Groups";
			case Generic<?> _ -> "Generics";
			default -> null;
		};
	}

	private static TypeParameter parameterize(final TypeParameter baseType,
											  final List<? extends TypeName> parameters)
	{
		if (parameters.isEmpty())
		{
			return baseType;
		}

		if (baseType instanceof TypeParameter.SimpleTypeParameter simple)
		{
			return TypeParameter.of(simple.raw(), parameters);
		}
		if (baseType instanceof TypeParameter.CombinedTypeParameter combined)
		{
			return TypeParameter.of(combined.raw(), parameters);
		}
		return baseType;
	}

	public record GenericResolution(TypeParameter resolvedType, TypeParameter rawType, boolean containsGeneric) {}

	public static TypeParameter resolveGenericBinding(final Generic<?> generic, final Group<?> owner)
	{
		return resolveGenericBinding(generic, owner, true);
	}

	public static TypeParameter resolveGenericBinding(final Generic<?> generic,
													  final Group<?> owner,
													  final boolean boxPrimitive)
	{
		final var boundType = resolveGenericBindingType(generic, owner);
		if (boundType == null) return null;
		final var bound = resolveSimpleType(boundType);
		if (!boxPrimitive)
		{
			return bound;
		}
		return bound != null ? boxIfPrimitive(bound) : null;
	}

	public static Type<?> resolveGenericBindingType(final Generic<?> generic, final Group<?> owner)
	{
		return resolveGenericBindingType(generic, owner, owner);
	}

	private static Type<?> resolveGenericBindingType(final Generic<?> generic,
													 final Group<?> current,
													 final Group<?> root)
	{
		if (current == generic.lmContainer())
		{
			return null;
		}

		for (final Include<?> include : current.includes())
		{
			final var includeGroup = (Group<?>) include.group();
			if (includeGroup == generic.lmContainer())
			{
				final var index = includeGroup.generics().indexOf(generic);
				if (index >= 0 && index < include.parameters().size())
				{
					final var parameter = include.parameters().get(index);
					return parameter.type();
				}
			}

			final var nested = resolveGenericBindingType(generic, includeGroup, root);
			if (nested != null)
			{
				return nested;
			}
		}

		return null;
	}

	private static TypeParameter boxIfPrimitive(final TypeParameter type)
	{
		if (type.parameters().isEmpty())
		{
			final var typeName = type.parametrized();
			if (typeName.isPrimitive())
			{
				return new TypeParameter.SimpleType(typeName.box());
			}
		}
		return type;
	}
}
