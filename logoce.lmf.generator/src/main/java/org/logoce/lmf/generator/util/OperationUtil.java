package org.logoce.lmf.generator.util;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Include;
import org.logoce.lmf.model.lang.Operation;
import org.logoce.lmf.model.lang.OperationParameter;
import org.logoce.lmf.model.lang.Type;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.BoundType;

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
		if (operation.lmContainer() == owner)
		{
			final var inherited = findInheritedOperation(owner, operation);
			if (inherited != null && inherited != operation)
			{
				return resolveReturnType(inherited, owner);
			}
		}

		final var type = operation.returnType();
		if (type == null)
		{
			return TypeName.VOID;
		}
		final var baseType = resolveType(type, owner);
		return parameterize(baseType, operation.returnTypeParameters(), owner);
	}

	public static TypeName resolveParameterType(final OperationParameter parameter, final Group<?> owner)
	{
		final var baseType = resolveType(parameter.type(), owner);
		return parameterize(baseType, parameter.parameters(), owner);
	}

	private static TypeName parameterize(final TypeParameter baseType,
										 final List<GenericParameter> params,
										 final Group<?> owner)
	{
		if (params == null || params.isEmpty())
		{
			return baseType.parametrized();
		}

		final var parameterTypes = params.stream()
										 .map(param -> resolveParameterType(param, owner))
										 .toArray(TypeName[]::new);

		if (baseType.parameters().isEmpty())
		{
			return parameterTypes.length == 1 ? parameterTypes[0] : baseType.parametrized();
		}

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

	private static Operation findInheritedOperation(final Group<?> owner, final Operation operation)
	{
		for (final Include<?> include : owner.includes())
		{
			final var includeGroup = (Group<?>) include.group();
			for (final Operation inheritedOperation : includeGroup.operations())
			{
				if (inheritedOperation.name().equals(operation.name()))
				{
					return inheritedOperation;
				}
			}
			final var nested = findInheritedOperation(includeGroup, operation);
			if (nested != null)
			{
				return nested;
			}
		}
		return null;
	}

	private static TypeName resolveParameterType(final GenericParameter parameter, final Group<?> owner)
	{
		if (parameter.type() instanceof Generic<?> generic)
		{
			final var bound = TypeResolutionUtil.resolveGenericBinding(generic, owner, true);
			if (bound != null)
			{
				final var nested = parameter.parameters()
											.stream()
											.map(param -> resolveParameterType(param, owner))
											.toList();
				final var resolved = nested.isEmpty()
									 ? bound.parametrized().box()
									 : parameterize(bound, nested).box();
				return parameter.wildcard() ? wildcard(resolved, parameter.wildcardBoundType()) : resolved;
			}
		}
		return org.logoce.lmf.generator.util.GenericParameter.resolveParameterType(parameter);
	}

	private static TypeName parameterize(final TypeParameter baseType, final List<? extends TypeName> parameterTypes)
	{
		if (parameterTypes.isEmpty())
		{
			return baseType.parametrized();
		}
		if (baseType instanceof TypeParameter.SimpleTypeParameter simple)
		{
			return ParameterizedTypeName.get(simple.raw(), parameterTypes.toArray(TypeName[]::new));
		}
		if (baseType instanceof TypeParameter.CombinedTypeParameter combined)
		{
			return ParameterizedTypeName.get(combined.raw(), parameterTypes.toArray(TypeName[]::new));
		}

		throw new IllegalArgumentException("Type cannot be parameterized: " + baseType.getClass().getSimpleName());
	}

	private static TypeName wildcard(final TypeName type, final BoundType boundType)
	{
		return switch (boundType)
		{
			case Super -> com.squareup.javapoet.WildcardTypeName.supertypeOf(type);
			case Extends -> com.squareup.javapoet.WildcardTypeName.subtypeOf(type);
			case null -> com.squareup.javapoet.WildcardTypeName.subtypeOf(type);
		};
	}
}
