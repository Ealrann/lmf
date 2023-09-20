package isotropy.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeVariableName;
import isotropy.lmf.core.lang.Generic;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.lang.Type;
import isotropy.lmf.core.util.ModelUtils;

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
		final var model = (Model) ModelUtils.root(type);
		return ClassName.get(model.domain(), type.name());
	}
}
