package org.logoce.extender.impl.util;

import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.ModelExtender;
import org.logoce.extender.api.reflect.ConstructorHandle;
import org.logoce.extender.api.reflect.ReflectUtils;
import org.logoce.extender.impl.AdapterDescriptor;
import org.logoce.extender.impl.reflect.ExecutionHandleBuilder;
import org.logoce.extender.impl.reflect.constructor.ConstructorHandleBuilder;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ExtenderDescriptorBuilder
{
	private final Map<Module, MethodHandles.Lookup> lookupMap;

	public ExtenderDescriptorBuilder(final Map<Module, MethodHandles.Lookup> lookupMap)
	{
		this.lookupMap = lookupMap;
	}

	public <Extender extends IAdapter> Optional<AdapterDescriptor<Extender>> build(final Class<Extender> type)
	{
		final ModelExtender modelExtenderAnnotation = getModelAnnotation(type);

		try
		{
			final var constructorHandle = buildConstructorHandle(type);
			final var executionHandles = executionHandles(type);

			final var res = new AdapterDescriptor<>(constructorHandle, modelExtenderAnnotation, type, executionHandles);
			return Optional.of(res);
		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
			return Optional.empty();
		}
	}

	private List<AnnotationHandlesBuilder<?>> executionHandles(final Class<?> extenderClass)
	{
		return ReflectUtils.streamAnnotatedMethods(extenderClass)
						   .collect(Collectors.groupingBy(p -> p.annotation()
																.annotationType()))
						   .entrySet()
						   .stream()
						   .<AnnotationHandlesBuilder<?>>map(this::buildAnnotationHandlesBuilder)
						   .toList();
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> AnnotationHandlesBuilder<T> buildAnnotationHandlesBuilder(final Map.Entry<? extends Class<T>, List<ReflectUtils.AnnotatedMethod<?>>> entry)
	{
		final var methods = entry.getValue()
								 .stream()
								 .map(am -> (ReflectUtils.AnnotatedMethod<T>) am);
		final var executionMethods = methods.map(this::buildExecutionMethod)
											.filter(Optional::isPresent)
											.map(Optional::get)
											.toList();
		return new AnnotationHandlesBuilder<>(entry.getKey(), executionMethods);
	}

	private <T extends Annotation> Optional<AnnotationHandlesBuilder.ExecutionMethod<T>> buildExecutionMethod(final ReflectUtils.AnnotatedMethod<T> method)
	{
		try
		{
			final var module = method.method()
									 .getDeclaringClass()
									 .getModule();
			final var lookup = lookupMap.get(module);
			final var handleBuilder = ExecutionHandleBuilder.fromMethod(lookup, method.method());
			return Optional.of(new AnnotationHandlesBuilder.ExecutionMethod<>(method, handleBuilder));
		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
			return Optional.empty();
		}
	}

	private <Extender> ConstructorHandle<Extender> buildConstructorHandle(final Class<Extender> type) throws ReflectiveOperationException
	{
		final var module = type.getModule();
		final var lookup = lookupMap.get(module);
		final var constructor = gatherConstructor(type);
		return ConstructorHandleBuilder.fromMethod(lookup, constructor)
									   .build();
	}

	private static <Extender> ModelExtender getModelAnnotation(final Class<Extender> type)
	{
		final var adapterAnnotation = type.getAnnotation(ModelExtender.class);
		if (adapterAnnotation == null) throwNoPluginAnnotationError(type);
		return adapterAnnotation;
	}

	private static <Extender> Constructor<Extender> gatherConstructor(Class<Extender> type)
	{
		@SuppressWarnings("unchecked") final var constructors = (Constructor<Extender>[]) type.getDeclaredConstructors();
		if (constructors.length != 1)
		{
			throw new AssertionError("IAdapter [" + type.getSimpleName() + "] must have one and only one constructor.");
		}

		final Constructor<Extender> res = constructors[0];
		res.setAccessible(true);
		return res;
	}

	private static void throwNoPluginAnnotationError(Class<?> type) throws IllegalStateException
	{
		final String adapterName = type.getSimpleName();
		final String annotationName = ModelExtender.class.getSimpleName();
		final String message = "The class [%s] is not annoted with @%s";
		final String errorMessage = String.format(message, adapterName, annotationName);
		throw new IllegalStateException(errorMessage);
	}
}
