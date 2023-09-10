package org.logoce.extender.impl.reflect.constructor;

import org.logoce.extender.api.reflect.ConstructorHandle;
import org.logoce.extender.impl.reflect.util.MethodHandleContext;
import org.logoce.extender.impl.reflect.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.util.function.Function;

public final class ConstructorHandleParam1<T> implements ConstructorHandle<T>
{
	private final Function<Object, T> function;
	private final Constructor<T> constructor;

	private ConstructorHandleParam1(Function<Object, T> function, Constructor<T> constructor)
	{
		this.function = function;
		this.constructor = constructor;
	}

	@Override
	public T newInstance(Object... parameters)
	{
		return function.apply(parameters[0]);
	}

	@Override
	public Constructor<T> getJavaConstructor()
	{
		return constructor;
	}

	public static final class Builder<T> extends ConstructorHandleBuilder<T>
	{
		private final ConstructorHandle<T> handle;

		public Builder(final MethodHandleContext context, Constructor<T> constructor) throws Throwable
		{
			final Function<Object, T> function = ReflectionUtil.createFunction(context);
			handle = new ConstructorHandleParam1<>(function, constructor);
		}

		@Override
		public ConstructorHandle<T> build()
		{
			return handle;
		}
	}
}
