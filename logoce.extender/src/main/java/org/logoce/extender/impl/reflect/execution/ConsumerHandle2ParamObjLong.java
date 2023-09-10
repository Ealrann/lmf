package org.logoce.extender.impl.reflect.execution;

import org.logoce.extender.api.reflect.ConsumerHandle;
import org.logoce.extender.impl.reflect.util.MethodHandleContext;

import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.ObjLongConsumer;

public final class ConsumerHandle2ParamObjLong implements ConsumerHandle
{
	private static final MethodType METHOD_TYPE = MethodType.methodType(Void.TYPE, Object.class, Long.TYPE);

	private final ObjLongConsumer<Object> consumer;

	private ConsumerHandle2ParamObjLong(ObjLongConsumer<Object> consumer)
	{
		this.consumer = consumer;
	}

	@Override
	public void invoke(Object... parameters)
	{
		consumer.accept(parameters[0], (long) parameters[1]);
	}

	@Override
	public Object getLambdaFunction()
	{
		return consumer;
	}

	public static final class Builder extends ConsumerHandleBuilder
	{
		private final MethodHandle factory;

		public Builder(final MethodHandleContext context) throws LambdaConversionException
		{
			final var factoryType = MethodType.methodType(ObjLongConsumer.class, context.declaringClass());
			final var targetType = context.methodHandle().type().dropParameterTypes(0, 1);
			final var site = LambdaMetafactory.metafactory(context.privateLookup(),
														   "accept",
														   factoryType,
														   METHOD_TYPE,
														   context.methodHandle(),
														   targetType);
			factory = site.getTarget();
		}

		@Override
		@SuppressWarnings("unchecked")
		public ConsumerHandle build(Object target)
		{
			try
			{
				final var consumer = (ObjLongConsumer<Object>) factory.invoke(target);
				return new ConsumerHandle2ParamObjLong(consumer);

			}
			catch (final Throwable e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}

	public static final class StaticBuilder extends ConsumerHandleBuilder
	{
		private final ConsumerHandle handle;

		@SuppressWarnings("unchecked")
		public StaticBuilder(final MethodHandleContext context) throws Throwable
		{
			final var site = LambdaMetafactory.metafactory(context.privateLookup(),
														   "accept",
														   MethodType.methodType(ObjLongConsumer.class),
														   METHOD_TYPE,
														   context.methodHandle(),
														   context.methodHandle().type());

			final var consumer = (ObjLongConsumer<Object>) site.getTarget().invoke();
			handle = new ConsumerHandle2ParamObjLong(consumer);
		}

		@Override
		public ConsumerHandle build(Object target)
		{
			return handle;
		}
	}
}
