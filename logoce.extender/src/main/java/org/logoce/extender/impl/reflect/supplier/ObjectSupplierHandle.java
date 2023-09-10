package org.logoce.extender.impl.reflect.supplier;

import org.logoce.extender.api.reflect.SupplierHandle;
import org.logoce.extender.impl.reflect.util.MethodHandleContext;
import org.logoce.extender.impl.reflect.util.ReflectionUtil;

import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

public final class ObjectSupplierHandle implements SupplierHandle
{
	private final Supplier<Object> supplier;

	private ObjectSupplierHandle(Supplier<Object> supplier)
	{
		this.supplier = supplier;
	}

	@Override
	public Object invoke()
	{
		return supplier.get();
	}

	@Override
	public Object getLambdaFunction()
	{
		return supplier;
	}

	public static final class Builder extends SupplierHandleBuilder
	{
		private final MethodHandle factory;

		public Builder(final MethodHandleContext context) throws LambdaConversionException
		{
			factory = ReflectionUtil.createSupplierFactory(context);
		}

		@Override
		@SuppressWarnings("unchecked")
		public ObjectSupplierHandle build(Object target)
		{
			try
			{
				final var supplier = (Supplier<Object>) factory.invoke(target);
				return new ObjectSupplierHandle(supplier);

			}
			catch (final Throwable e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}

	public static final class StaticBuilder extends SupplierHandleBuilder
	{
		private final ObjectSupplierHandle handle;

		public StaticBuilder(final MethodHandleContext context) throws Throwable
		{
			final var supplier = ReflectionUtil.createSupplier(context);
			handle = new ObjectSupplierHandle(supplier);
		}

		@Override
		public ObjectSupplierHandle build(Object target)
		{
			return handle;
		}
	}
}
