package org.logoce.lmf.generator.util;

import com.squareup.javapoet.ParameterizedTypeName;
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
		final var parameter = extension.parameter();

		if (parameter != null && baseType instanceof TypeParameter.SimpleType)
		{
			throw new IllegalArgumentException("Cannot parameterize non-class type: " + type.name());
		}

		final var resolvedType = parameter != null
								 ? parameterize(baseType, resolveParameterType(parameter))
								 : baseType.parametrized();

		return resolvedType.box();
	}

	private static TypeName parameterize(final TypeParameter baseType, final TypeName parameterType)
	{
		if (baseType instanceof TypeParameter.SimpleTypeParameter simple)
		{
			return ParameterizedTypeName.get(simple.raw(), parameterType.box());
		}
		if (baseType instanceof TypeParameter.CombinedTypeParameter combined)
		{
			return ParameterizedTypeName.get(combined.raw(), parameterType.box());
		}

		throw new IllegalArgumentException("Type cannot be parameterized: " + baseType.getClass().getSimpleName());
	}

	private static TypeName resolveParameterType(final org.logoce.lmf.model.lang.GenericParameter parameter)
	{
		final var type = parameter.type();
		final var nestedParameter = parameter.parameter();

		if (type instanceof Generic<?> genericType)
		{
			if (nestedParameter != null)
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
		final var nestedType = nestedParameter != null ? resolveParameterType(nestedParameter) : null;

		final var resolvedType = nestedType != null
								 ? parameterize(baseType, nestedType)
								 : baseType.parametrized().box();

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
