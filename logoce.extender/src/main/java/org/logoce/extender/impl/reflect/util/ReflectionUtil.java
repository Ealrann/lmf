package org.logoce.extender.impl.reflect.util;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.*;

public final class ReflectionUtil
{
	private static final MethodType TYPE_R_RUNNABLE = MethodType.methodType(Runnable.class);
	private static final MethodType TYPE_R_CONSUMER = MethodType.methodType(Consumer.class);
	private static final MethodType TYPE_R_FUNCTION = MethodType.methodType(Function.class);
	private static final MethodType TYPE_R_BIFUNCTION = MethodType.methodType(BiFunction.class);
	private static final MethodType TYPE_R_SUPPLIER = MethodType.methodType(Supplier.class);
	private static final MethodType TYPE_R_BICONSUMER = MethodType.methodType(BiConsumer.class);
	private static final MethodType TYPE_VOID_VOID = MethodType.methodType(Void.TYPE);
	private static final MethodType TYPE_OBJECT_VOID = MethodType.methodType(Object.class);
	private static final MethodType TYPE_VOID_OBJECT = MethodType.methodType(Void.TYPE, Object.class);
	private static final MethodType TYPE_VOID_OBJECT_OBJECT = MethodType.methodType(Void.TYPE,
																					Object.class,
																					Object.class);
	private static final String RUNNABLE_EXEC_METHOD = "run";
	private static final String CONSUMER_EXEC_METHOD = "accept";
	private static final String FUNCTION_EXEC_METHOD = "apply";
	private static final String SUPPLIER_EXEC_METHOD = "get";

	private static final Module MODULE = ReflectionUtil.class.getModule();

	public static MethodHandleContext unreflect(final Constructor<?> constructor,
												final MethodHandles.Lookup lookup) throws IllegalAccessException
	{
		final Class<?> declaringClass = constructor.getDeclaringClass();
		final var privateLookup = reachPrivateLookup(lookup, declaringClass);
		final var constructorHandle = lookup.unreflectConstructor(constructor);

		return new MethodHandleContext(constructorHandle, declaringClass, privateLookup);
	}

	public static MethodHandleContext unreflect(final Method method,
												final MethodHandles.Lookup lookup) throws IllegalAccessException
	{
		final var modifiers = method.getModifiers();
		final var staticMethod = Modifier.isStatic(modifiers);
		final var declaringClass = method.getDeclaringClass();
		final var privateLookup = reachPrivateLookup(lookup, declaringClass);

		final var methodHandle = staticMethod
				? privateLookup.unreflect(method) : privateLookup.unreflectSpecial(method, declaringClass);

		return new MethodHandleContext(methodHandle, declaringClass, privateLookup);
	}

	private static Lookup reachPrivateLookup(final MethodHandles.Lookup lookup,
											final Class<?> targetClass) throws IllegalAccessException
	{
		final var otherModule = targetClass.getModule();
		final boolean canRead = MODULE.canRead(otherModule);
		if (canRead == false)
		{
			MODULE.addReads(otherModule);
		}

		return MethodHandles.privateLookupIn(targetClass, lookup);
	}

	public static Runnable createRunnable(final MethodHandleContext context) throws Throwable
	{
		final CallSite site = LambdaMetafactory.metafactory(context.privateLookup(),
															RUNNABLE_EXEC_METHOD,
															TYPE_R_RUNNABLE,
															TYPE_VOID_VOID,
															context.methodHandle(),
															context.methodHandle().type());
		return (Runnable) site.getTarget().invokeExact();
	}

	public static MethodHandle createRunnableFactory(final MethodHandleContext context) throws LambdaConversionException
	{
		final var factoryType = MethodType.methodType(Runnable.class, context.declaringClass());
		final var site = LambdaMetafactory.metafactory(context.privateLookup(),
													   RUNNABLE_EXEC_METHOD,
													   factoryType,
													   TYPE_VOID_VOID,
													   context.methodHandle(),
													   TYPE_VOID_VOID);

		return site.getTarget();
	}

	@SuppressWarnings("unchecked")
	public static <T> Consumer<T> createConsumer(final MethodHandleContext context) throws Throwable
	{
		final var site = LambdaMetafactory.metafactory(context.privateLookup(),
													   CONSUMER_EXEC_METHOD,
													   TYPE_R_CONSUMER,
													   TYPE_VOID_OBJECT,
													   context.methodHandle(),
													   context.methodHandle().type());
		return (Consumer<T>) site.getTarget().invokeExact();
	}

	public static MethodHandle createConsumerFactory(final MethodHandleContext context) throws LambdaConversionException
	{
		final var factoryType = MethodType.methodType(Consumer.class, context.declaringClass());
		final var targetType = context.methodHandle().type().dropParameterTypes(0, 1);
		final var site = LambdaMetafactory.metafactory(context.privateLookup(),
													   CONSUMER_EXEC_METHOD,
													   factoryType,
													   TYPE_VOID_OBJECT,
													   context.methodHandle(),
													   targetType);
		return site.getTarget();
	}

	@SuppressWarnings("unchecked")
	public static <T> Supplier<T> createSupplier(final MethodHandleContext context) throws Throwable
	{
		final CallSite site = LambdaMetafactory.metafactory(context.privateLookup(),
															SUPPLIER_EXEC_METHOD,
															TYPE_R_SUPPLIER,
															TYPE_OBJECT_VOID,
															context.methodHandle(),
															context.methodHandle().type());
		return (Supplier<T>) site.getTarget().invokeExact();
	}

	public static MethodHandle createSupplierFactory(final MethodHandleContext context) throws LambdaConversionException
	{
		final var factoryType = MethodType.methodType(Supplier.class, context.declaringClass());
		final var site = LambdaMetafactory.metafactory(context.privateLookup(),
													   SUPPLIER_EXEC_METHOD,
													   factoryType,
													   TYPE_OBJECT_VOID,
													   context.methodHandle(),
													   TYPE_OBJECT_VOID);
		return site.getTarget();
	}

	@SuppressWarnings("unchecked")
	public static <T> BiConsumer<T, Object> createBiConsumer(final MethodHandleContext context) throws Throwable
	{
		final var site = LambdaMetafactory.metafactory(context.privateLookup(),
													   CONSUMER_EXEC_METHOD,
													   TYPE_R_BICONSUMER,
													   TYPE_VOID_OBJECT_OBJECT,
													   context.methodHandle(),
													   context.methodHandle().type());
		return (BiConsumer<T, Object>) site.getTarget().invokeExact();
	}

	public static MethodHandle createBiConsumerFactory(final MethodHandleContext context) throws LambdaConversionException
	{
		final var factoryType = MethodType.methodType(BiConsumer.class, context.declaringClass());
		final var targetType = context.methodHandle().type().dropParameterTypes(0, 1);
		final var site = LambdaMetafactory.metafactory(context.privateLookup(),
													   CONSUMER_EXEC_METHOD,
													   factoryType,
													   TYPE_VOID_OBJECT_OBJECT,
													   context.methodHandle(),
													   targetType);
		return site.getTarget();
	}

	@SuppressWarnings("unchecked")
	public static <T> Function<Object, T> createFunction(final MethodHandleContext context) throws Throwable
	{
		final CallSite site = LambdaMetafactory.metafactory(context.privateLookup(),
															FUNCTION_EXEC_METHOD,
															TYPE_R_FUNCTION,
															context.methodHandle().type().generic(),
															context.methodHandle(),
															context.methodHandle().type());
		return (Function<Object, T>) site.getTarget().invokeExact();
	}

	@SuppressWarnings("unchecked")
	public static <T> BiFunction<Object, Object, T> createBiFunction(final MethodHandleContext context) throws Throwable
	{
		final CallSite site = LambdaMetafactory.metafactory(context.privateLookup(),
															FUNCTION_EXEC_METHOD,
															TYPE_R_BIFUNCTION,
															context.methodHandle().type().generic(),
															context.methodHandle(),
															context.methodHandle().type());
		return (BiFunction<Object, Object, T>) site.getTarget().invokeExact();
	}
}
