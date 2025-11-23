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
		final var parameter = extension.parameter();
		final var extensionType = parameter != null
								  ? TypeParameter.of(baseType.raw(), resolveParameterType(parameter))
								  : baseType;

		return extensionType.parametrized().box();
	}

	private static TypeName resolveParameterType(final org.logoce.lmf.model.lang.GenericParameter parameter)
	{
		final var baseType = TypeResolutionUtil.resolveSimpleType(parameter.type());
		final var nestedParameter = parameter.parameter();
		final var nestedType = nestedParameter != null
							   ? resolveParameterType(nestedParameter)
							   : null;

		final var parameterType = nestedType != null
								  ? TypeParameter.of(baseType.raw(), nestedType)
								  : baseType;

		final var resolvedType = parameterType.parametrized().box();
		if (!parameter.wildcard())
		{
			return resolvedType;
		}

		final var wildcardBoundType = parameter.wildcardBoundType();
		return switch (wildcardBoundType)
		{
			case Super -> WildcardTypeName.supertypeOf(resolvedType);
			case Extends -> WildcardTypeName.subtypeOf(resolvedType);
			case null -> WildcardTypeName.subtypeOf(resolvedType);
		};
	}
}
