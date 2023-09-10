package org.logoce.extender.impl.reflect.supplier;

import org.logoce.extender.api.reflect.SupplierHandle;
import org.logoce.extender.impl.reflect.ExecutionHandleBuilder;
import org.logoce.extender.impl.reflect.util.MethodHandleContext;
import org.logoce.extender.impl.reflect.util.ReflectionUtil;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract sealed class SupplierHandleBuilder extends ExecutionHandleBuilder permits BooleanSupplierHandle.Builder,
																						  BooleanSupplierHandle.StaticBuilder,
																						  ObjectSupplierHandle.Builder,
																						  ObjectSupplierHandle.StaticBuilder
{
	public static final SupplierHandleBuilder fromMethod(final MethodHandles.Lookup lookup,
														 final Method method) throws ReflectiveOperationException
	{
		final var isStatic = Modifier.isStatic(method.getModifiers());

		try
		{
			final var methodHandle = ReflectionUtil.unreflect(method, lookup);
			return buildNoArgs(methodHandle, isStatic);
		}
		catch (final Throwable e)
		{
			throw new ReflectiveOperationException("Cannot create Handle for Method: " + method, e);
		}
	}

	private static SupplierHandleBuilder buildNoArgs(final MethodHandleContext methodHandleContext,
													 final boolean isStatic) throws Throwable
	{
		if (methodHandleContext.methodHandle().type().returnType() == Boolean.TYPE)
		{
			if (isStatic) return new BooleanSupplierHandle.StaticBuilder(methodHandleContext);
			else return new BooleanSupplierHandle.Builder(methodHandleContext);
		}
		else
		{
			if (isStatic) return new ObjectSupplierHandle.StaticBuilder(methodHandleContext);
			else return new ObjectSupplierHandle.Builder(methodHandleContext);
		}
	}

	@Override
	public abstract SupplierHandle build(Object target);
}