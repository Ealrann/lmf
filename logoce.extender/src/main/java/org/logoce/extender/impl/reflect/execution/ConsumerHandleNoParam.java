package org.logoce.extender.impl.reflect.execution;

import org.logoce.extender.api.reflect.ConsumerHandle;
import org.logoce.extender.impl.reflect.util.MethodHandleContext;
import org.logoce.extender.impl.reflect.util.ReflectionUtil;

import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.MethodHandle;

public final class ConsumerHandleNoParam implements ConsumerHandle
{
	private final Runnable runnable;

	private ConsumerHandleNoParam(Runnable runnable)
	{
		this.runnable = runnable;
	}

	@Override
	public void invoke(Object... parameters)
	{
		runnable.run();
	}

	@Override
	public Object getLambdaFunction()
	{
		return runnable;
	}

	public static final class Builder extends ConsumerHandleBuilder
	{
		private final MethodHandle factory;

		public Builder(final MethodHandleContext context) throws LambdaConversionException
		{
			factory = ReflectionUtil.createRunnableFactory(context);
		}

		@Override
		public ConsumerHandle build(Object target)
		{
			try
			{
				final var runnable = (Runnable) factory.invoke(target);
				return new ConsumerHandleNoParam(runnable);

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
			final var runnable = ReflectionUtil.createRunnable(context);
			handle = new ConsumerHandleNoParam(runnable);
		}

		@Override
		public ConsumerHandle build(Object target)
		{
			return handle;
		}
	}
}
