package org.logoce.extender.impl.reflect;

import org.logoce.extender.api.reflect.IExecutionHandleBuilder;
import org.logoce.extender.impl.reflect.execution.ConsumerHandleBuilder;
import org.logoce.extender.impl.reflect.supplier.SupplierHandleBuilder;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public abstract sealed class ExecutionHandleBuilder implements IExecutionHandleBuilder permits ConsumerHandleBuilder,
																							   SupplierHandleBuilder
{
	public static ExecutionHandleBuilder fromMethod(final MethodHandles.Lookup lookup,
													final Method method) throws ReflectiveOperationException
	{
		if (method.getReturnType() != Void.TYPE)
		{
			return SupplierHandleBuilder.fromMethod(lookup, method);
		}
		else
		{
			return ConsumerHandleBuilder.fromMethod(lookup, method);
		}
	}
}
