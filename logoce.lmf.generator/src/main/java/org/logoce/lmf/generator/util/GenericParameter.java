package org.logoce.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeVariableName;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Type;
import org.logoce.lmf.model.util.ModelUtils;

public record GenericParameter(TypeVariableName raw, TypeVariableName defined)
{
	public static GenericParameter fromGeneric(Generic<?> generic)
	{
		final var type = generic.type();
		final var typeVariableNameRaw = TypeVariableName.get(generic.name());
		final var typeVariableNameTyped = type != null
										  ? TypeVariableName.get(generic.name(), resolveType(type))
										  : typeVariableNameRaw;

		return new GenericParameter(typeVariableNameRaw, typeVariableNameTyped);
	}

	private static ClassName resolveType(final Type<?> type)
	{
		final var model = (MetaModel) ModelUtils.root(type);
		return ClassName.get(model.domain(), type.name());
	}
}
