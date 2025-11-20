package org.logoce.lmf.generator.util;

import com.squareup.javapoet.TypeName;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Operation;
import org.logoce.lmf.model.lang.OperationParameter;
import org.logoce.lmf.model.util.ModelUtils;

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
		final var type = operation.type();
		if (type == null)
		{
			return TypeName.VOID;
		}
		return TypeResolutionUtil.resolveSimpleType(type).parametrized();
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

	private static String signature(final OperationParameter parameter)
	{
		return TypeResolutionUtil.resolveSimpleType(parameter.type()).parametrized().toString();
	}
}
