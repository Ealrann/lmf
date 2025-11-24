package org.logoce.lmf.generator.util;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Operation;
import org.logoce.lmf.model.lang.OperationParameter;
import org.logoce.lmf.model.util.ModelUtils;
import org.logoce.lmf.generator.util.TypeParameter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class OperationUtil
{
	private OperationUtil()
	{
	}

	public static List<Operation> collectOperations(final Group<?> group)
	{
		final Map<String, Operation> operations = new LinkedHashMap<>();
		ModelUtils.streamHierarchy(group)
				  .flatMap(g -> g.operations().stream())
				  .forEach(operation -> operations.put(signature(operation), operation));
		return List.copyOf(operations.values());
	}

	public static TypeName resolveReturnType(final Operation operation)
	{
		final var type = operation.returnType();
		if (type == null)
		{
			return TypeName.VOID;
		}
		final var baseType = TypeResolutionUtil.resolveSimpleType(type);
		return parameterize(baseType, operation.returnTypeParameters());
	}

	private static String signature(final Operation operation)
	{
		final var parameters = operation.parameters()
										.stream()
										.map(OperationUtil::signature)
										.collect(Collectors.joining(","));
		final var returnType = resolveReturnType(operation).toString();
		return operation.name() + "(" + parameters + "):" + returnType;
	}

	public static TypeName resolveParameterType(final OperationParameter parameter)
	{
		final var baseType = TypeResolutionUtil.resolveSimpleType(parameter.type());
		return parameterize(baseType, parameter.parameters());
	}

	private static String signature(final OperationParameter parameter)
	{
		return resolveParameterType(parameter).toString();
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
}
