package org.logoce.extender.impl.reflect.constructor;

import org.logoce.extender.api.reflect.ConstructorHandle;
import org.logoce.extender.impl.reflect.util.ReflectionUtil;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

public abstract sealed class ConstructorHandleBuilder<T> permits ConstructorHandleNoParam.Builder,
																 ConstructorHandleParam1.Builder,
																 ConstructorHandleParam2.Builder,
																 ConstructorHandleParamN.Builder
{
	public static final <T> ConstructorHandleBuilder<T> fromMethod(final MethodHandles.Lookup lookup,
																   final Constructor<T> constructor) throws ReflectiveOperationException
	{
		final int paramCount = constructor.getParameterCount();

		try
		{
			final var context = ReflectionUtil.unreflect(constructor, lookup);
			return switch (paramCount)
					{
						case 0 -> new ConstructorHandleNoParam.Builder<>(context, constructor);
						case 1 -> new ConstructorHandleParam1.Builder<>(context, constructor);
						case 2 -> new ConstructorHandleParam2.Builder<>(context, constructor);
						default -> new ConstructorHandleParamN.Builder<>(context, constructor);
					};
		}
		catch (final Throwable e)
		{
			throw new ReflectiveOperationException("Cannot create Handle for Constructor: " + constructor, e);
		}
	}

	public abstract ConstructorHandle<T> build();
}
