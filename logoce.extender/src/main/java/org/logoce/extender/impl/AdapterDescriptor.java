package org.logoce.extender.impl;

import org.logoce.extender.api.*;
import org.logoce.extender.api.parameter.IParameterResolver;
import org.logoce.extender.api.reflect.ConstructorHandle;
import org.logoce.extender.ext.IAdaptableNameMatcher;
import org.logoce.extender.impl.util.AnnotationHandlesBuilder;
import org.logoce.extender.impl.util.ExtenderBuilder;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public final class AdapterDescriptor<Extender extends IAdapter> implements IAdapterDescriptor<Extender>
{
	private static final List<IAdaptableNameMatcher> nameMatchers = ServiceLoader.load(IAdaptableNameMatcher.class)
																				 .stream()
																				 .map(ServiceLoader.Provider::get)
																				 .toList();

	private final ModelExtender annotation;
	private final Class<Extender> extenderClass;
	private final ExtenderBuilder<Extender> extenderBuilder;
	private final List<AnnotationHandlesBuilder<?>> executionHandleBuilders;

	public AdapterDescriptor(final ConstructorHandle<Extender> constructorHandle,
							 final ModelExtender annotation,
							 final Class<Extender> extenderClass,
							 final List<AnnotationHandlesBuilder<?>> executionHandleBuilders)
	{
		this.annotation = annotation;
		this.extenderClass = extenderClass;
		this.extenderBuilder = new ExtenderBuilder<>(constructorHandle, extenderClass);
		this.executionHandleBuilders = List.copyOf(executionHandleBuilders);
	}

	@Override
	public ExtenderContext<Extender> newExtender(final IAdaptable target,
												 final Stream<? extends IParameterResolver> resolvers) throws ReflectiveOperationException
	{
		final var extender = extenderBuilder.build(target, resolvers);
		final var annotationHandles = executionHandleBuilders.stream()
															 .map(builder -> builder.build(extender))
															 .toList();
		return new ExtenderContext<>(extender, new AnnotationHandles(annotationHandles));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A extends Annotation> Stream<A> streamMethodAnnotations(final Class<A> annotationClass)
	{
		return executionHandleBuilders.stream()
									  .filter(builder -> builder.annotationClass() == annotationClass)
									  .map(builder -> (AnnotationHandlesBuilder<A>) builder)
									  .flatMap(AnnotationHandlesBuilder::streamAnnotations);
	}

	@Override
	public boolean match(final Class<? extends IAdapter> type)
	{
		return type.isAssignableFrom(extenderClass);
	}

	@Override
	public boolean match(final Class<? extends IAdapter> type, final String identifier)
	{
		return match(type) && Objects.equals(annotation.identifier(), identifier);
	}

	@Override
	public boolean isApplicable(final IAdaptable target)
	{
		final boolean res = isClassApplicable(target.getClass());

		if (res && annotation.name()
							 .isEmpty() == false)
		{
			return nameMatchers.stream()
							   .anyMatch(m -> m.match(target, annotation.name()));
		}

		return res;
	}

	private boolean isClassApplicable(final Class<?> classifier)
	{
		final var scope = annotation.scope();
		if (annotation.inherited())
		{
			return scope.isAssignableFrom(classifier);
		}
		else if (scope.isInterface())
		{
			return isDirectInterfaceOf(classifier, scope);
		}
		else
		{
			return scope.equals(classifier);
		}
	}

	private static boolean isDirectInterfaceOf(final Class<?> rootClass, final Class<?> _interface)
	{
		return Arrays.stream(rootClass.getInterfaces())
					 .anyMatch(i -> i == _interface);
	}

	@Override
	public boolean containsMethodAnnotation(final Class<? extends Annotation> annotationClass)
	{
		return executionHandleBuilders.stream()
									  .map(AnnotationHandlesBuilder::annotationClass)
									  .anyMatch(bAnnotationClass -> bAnnotationClass == annotationClass);
	}

	@Override
	public boolean containsClassAnnotation(final Class<? extends Annotation> annotationClass)
	{
		return extenderClass.isAnnotationPresent(annotationClass);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final AdapterDescriptor<?> that = (AdapterDescriptor<?>) o;
		return extenderClass.equals(that.extenderClass);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(extenderClass);
	}

	@Override
	public Class<Extender> extenderClass()
	{
		return extenderClass;
	}

	@Override
	public String toString()
	{
		return "ExtenderDescriptor{" + "extenderClass=" + extenderClass + '}';
	}
}
