package org.logoce.extender.impl.reflect.execution;

import org.logoce.extender.api.reflect.ConsumerHandle;
import org.logoce.extender.impl.reflect.util.MethodHandleContext;
import org.logoce.extender.impl.reflect.util.ReflectionUtil;

import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.MethodHandle;
import java.util.function.BiConsumer;

public final class ConsumerHandle2Param implements ConsumerHandle
{
	private final BiConsumer<Object, Object> consumer;

	private ConsumerHandle2Param(BiConsumer<Object, Object> consumer)
	{
		this.consumer = consumer;
	}

	@Override
	public void invoke(Object... parameters)
	{
		consumer.accept(parameters[0], parameters[1]);
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
			factory = ReflectionUtil.createBiConsumerFactory(context);
		}

		@Override
		@SuppressWarnings("unchecked")
		public ConsumerHandle build(Object target)
		{
			try
			{
				final var consumer = (BiConsumer<Object, Object>) factory.invoke(target);
				return new ConsumerHandle2Param(consumer);

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

		public StaticBuilder(final MethodHandleContext context) throws Throwable
		{
			final var consumer = ReflectionUtil.createBiConsumer(context);
			handle = new ConsumerHandle2Param(consumer);
		}

		@Override
		public ConsumerHandle build(Object target)
		{
			return handle;
		}
	}
}
