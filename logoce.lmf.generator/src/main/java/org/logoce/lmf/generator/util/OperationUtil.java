package org.logoce.lmf.generator.util;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Operation;
import org.logoce.lmf.model.lang.OperationParameter;
import org.logoce.lmf.model.lang.Type;
import org.logoce.lmf.model.lang.Generic;

import java.util.List;

public final class OperationUtil
{
	private OperationUtil()
	{
	}

	/**
	 * Returns the operations declared directly on the given group.
	 */
	public static List<Operation> collectOperations(final Group<?> owner)
	{
		return List.copyOf(owner.operations());
	}

	public static TypeName resolveReturnType(final Operation operation)
	{
		return resolveReturnType(operation, (Group<?>) operation.lmContainer());
	}

	public static TypeName resolveReturnType(final Operation operation, final Group<?> owner)
	{
		final var type = operation.returnType();
		if (type == null)
		{
			return TypeName.VOID;
		}
		final var baseType = resolveType(type, owner);
		return parameterize(baseType, operation.returnTypeParameters());
	}

	public static TypeName resolveParameterType(final OperationParameter parameter, final Group<?> owner)
	{
		final var baseType = resolveType(parameter.type(), owner);
		return parameterize(baseType, parameter.parameters());
	}

	private static TypeName parameterize(final TypeParameter baseType, final List<GenericParameter> params)
	{
		if (params == null || params.isEmpty())
		{
			return baseType.parametrized();
		}
		final var parameterTypes = params.stream()
										 .map(org.logoce.lmf.generator.util.GenericParameter::resolveParameterType)
										 .toArray(TypeName[]::new);

		if (baseType instanceof TypeParameter.SimpleTypeParameter simple)
		{
			return ParameterizedTypeName.get(simple.raw(), parameterTypes);
		}
		if (baseType instanceof TypeParameter.CombinedTypeParameter combined)
		{
			return ParameterizedTypeName.get(combined.raw(), parameterTypes);
		}

		throw new IllegalArgumentException("Type cannot be parameterized: " + baseType.getClass().getSimpleName());
	}

	private static TypeParameter resolveType(final Type<?> type, final Group<?> owner)
	{
		if (type instanceof Generic<?> generic)
		{
			final var bound = TypeResolutionUtil.resolveGenericBinding(generic, owner, false);
			if (bound != null)
			{
				return bound;
			}
		}
		return TypeResolutionUtil.resolveSimpleType(type);
	}
}
