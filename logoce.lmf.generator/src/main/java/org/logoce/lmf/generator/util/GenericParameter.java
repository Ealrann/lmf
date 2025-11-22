package org.logoce.lmf.generator.util;

import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.TypeName;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.Type;

public record GenericParameter(TypeVariableName raw, TypeVariableName defined)
{
	public static GenericParameter fromGeneric(Generic<?> generic)
	{
		final var extension = generic.extension();
		final var type = extension != null ? extension.type() : null;
		final var typeVariableNameRaw = TypeVariableName.get(generic.name());
		final var typeVariableNameTyped = type != null
										  ? TypeVariableName.get(generic.name(), resolveType(type))
										  : typeVariableNameRaw;

		return new GenericParameter(typeVariableNameRaw, typeVariableNameTyped);
	}

	private static TypeName resolveType(final Type<?> type)
	{
		return TypeResolutionUtil.resolveSimpleType(type).parametrized().box();
	}
}
