package org.logoce.lmf.generator.util;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;
import org.logoce.lmf.model.lang.BoundType;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.GenericExtension;

public record GenericParameter(TypeVariableName raw, TypeVariableName defined)
{
	public static GenericParameter fromGeneric(Generic<?> generic)
	{
		final var extension = generic.extension();
		final var type = extension != null ? resolveExtensionType(extension) : null;
		final var typeVariableNameRaw = TypeVariableName.get(generic.name());
		final var typeVariableNameTyped = type != null
										  ? TypeVariableName.get(generic.name(), type)
										  : typeVariableNameRaw;

		return new GenericParameter(typeVariableNameRaw, typeVariableNameTyped);
	}

	private static TypeName resolveExtensionType(final GenericExtension extension)
	{
		final var type = extension.type();
		if (type == null)
		{
			return null;
		}

		final var baseType = TypeResolutionUtil.resolveSimpleType(type);
		final var parameters = extension.parameters()
										.stream()
										.map(GenericParameter::resolveParameterType)
										.toList();

		if (!parameters.isEmpty() && baseType instanceof TypeParameter.SimpleType)
		{
			throw new IllegalArgumentException("Cannot parameterize non-class type: " + type.name());
		}

		final var resolvedType = parameters.isEmpty()
								 ? baseType.parametrized()
								 : parameterize(baseType, parameters);

		return resolvedType.box();
	}

	private static TypeName parameterize(final TypeParameter baseType, final java.util.List<? extends TypeName> parameterTypes)
	{
		if (baseType instanceof TypeParameter.SimpleTypeParameter simple)
		{
			return com.squareup.javapoet.ParameterizedTypeName.get(simple.raw(),
																  parameterTypes.stream().map(TypeName::box).toArray(TypeName[]::new));
		}
		if (baseType instanceof TypeParameter.CombinedTypeParameter combined)
		{
			return com.squareup.javapoet.ParameterizedTypeName.get(combined.raw(),
																   parameterTypes.stream().map(TypeName::box).toArray(TypeName[]::new));
		}

		throw new IllegalArgumentException("Type cannot be parameterized: " + baseType.getClass().getSimpleName());
	}

	public static TypeName resolveParameterType(final org.logoce.lmf.model.lang.GenericParameter parameter)
	{
		final var type = parameter.type();
		final var nestedParameters = parameter.parameters();

		if (type instanceof Generic<?> genericType)
		{
			if (!nestedParameters.isEmpty())
			{
				throw new IllegalArgumentException("Generic type parameter cannot declare nested parameters: " +
												   genericType.name());
			}
			final var typeVar = TypeVariableName.get(genericType.name());
			return parameter.wildcard()
				   ? wildcard(typeVar, parameter.wildcardBoundType())
				   : typeVar;
		}

		final var baseType = TypeResolutionUtil.resolveSimpleType(type);
		final var nestedTypes = nestedParameters.stream()
												.map(GenericParameter::resolveParameterType)
												.toList();

		final var resolvedType = nestedTypes.isEmpty()
								 ? baseType.parametrized().box()
								 : parameterize(baseType, nestedTypes).box();

		return parameter.wildcard()
			   ? wildcard(resolvedType, parameter.wildcardBoundType())
			   : resolvedType;
	}

	private static TypeName wildcard(final TypeName type, final BoundType boundType)
	{
		return switch (boundType)
		{
			case Super -> WildcardTypeName.supertypeOf(type);
			case Extends -> WildcardTypeName.subtypeOf(type);
			case null -> WildcardTypeName.subtypeOf(type);
		};
	}
}
