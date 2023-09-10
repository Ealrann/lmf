package org.logoce.extender.impl.reflect.execution;

import org.logoce.extender.api.reflect.ConsumerHandle;
import org.logoce.extender.impl.reflect.ExecutionHandleBuilder;
import org.logoce.extender.impl.reflect.util.MethodHandleContext;
import org.logoce.extender.impl.reflect.util.ReflectionUtil;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract sealed class ConsumerHandleBuilder extends ExecutionHandleBuilder permits ConsumerHandleNoParam.StaticBuilder,
																						  ConsumerHandleNoParam.Builder,
																						  ConsumerHandle1ParamLong.StaticBuilder,
																						  ConsumerHandle1ParamLong.Builder,
																						  ConsumerHandle1Param.StaticBuilder,
																						  ConsumerHandle1Param.Builder,
																						  ConsumerHandle2ParamObjLong.StaticBuilder,
																						  ConsumerHandle2ParamObjLong.Builder,
																						  ConsumerHandle2Param.StaticBuilder,
																						  ConsumerHandle2Param.Builder
{
	public static final ConsumerHandleBuilder fromMethod(final MethodHandles.Lookup lookup,
														 final Method method) throws ReflectiveOperationException
	{
		final int paramCount = method.getParameterCount();
		final var isStatic = Modifier.isStatic(method.getModifiers());

		try
		{
			final var context = ReflectionUtil.unreflect(method, lookup);
			return switch (paramCount)
					{
						case 0 -> buildNoArgs(context, isStatic);
						case 1 -> build1Args(context, isStatic);
						case 2 -> build2Args(context, isStatic);
						default -> null;
					};
		}
		catch (final Throwable e)
		{
			throw new ReflectiveOperationException("Cannot create Handle for Method: " + method, e);
		}
	}

	private static ConsumerHandleBuilder buildNoArgs(final MethodHandleContext context,
													 final boolean isStatic) throws Throwable
	{
		if (isStatic) return new ConsumerHandleNoParam.StaticBuilder(context);
		else return new ConsumerHandleNoParam.Builder(context);
	}

	private static ConsumerHandleBuilder build1Args(final MethodHandleContext context,
													final boolean isStatic) throws Throwable
	{
		if (context.methodHandle().type().lastParameterType() == Long.TYPE)
		{
			if (isStatic) return new ConsumerHandle1ParamLong.StaticBuilder(context);
			else return new ConsumerHandle1ParamLong.Builder(context);
		}
		else
		{
			if (isStatic) return new ConsumerHandle1Param.StaticBuilder(context);
			else return new ConsumerHandle1Param.Builder(context);
		}
	}

	private static ConsumerHandleBuilder build2Args(final MethodHandleContext context,
													final boolean isStatic) throws Throwable
	{
		if (context.methodHandle().type().lastParameterType() == Long.TYPE)
		{
			if (isStatic) return new ConsumerHandle2ParamObjLong.StaticBuilder(context);
			else return new ConsumerHandle2ParamObjLong.Builder(context);
		}
		else
		{
			if (isStatic) return new ConsumerHandle2Param.StaticBuilder(context);
			else return new ConsumerHandle2Param.Builder(context);
		}
	}

	@Override
	public abstract ConsumerHandle build(Object target);
}
