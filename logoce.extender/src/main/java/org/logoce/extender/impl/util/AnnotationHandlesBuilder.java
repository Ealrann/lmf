package org.logoce.extender.impl.util;

import org.logoce.extender.api.AnnotationHandleGroup;
import org.logoce.extender.api.IAdapterHandle;
import org.logoce.extender.api.reflect.IExecutionHandleBuilder;
import org.logoce.extender.api.reflect.ReflectUtils;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;

public final class AnnotationHandlesBuilder<T extends Annotation>
{
	private final Class<T> annotationClass;
	private final List<ExecutionMethod<T>> methods;

	public AnnotationHandlesBuilder(final Class<T> annotationClass, final List<ExecutionMethod<T>> methods)
	{
		this.annotationClass = annotationClass;
		this.methods = List.copyOf(methods);
	}

	public Class<T> annotationClass()
	{
		return annotationClass;
	}

	public Stream<T> streamAnnotations()
	{
		return methods.stream()
					  .map(m -> m.method.annotation());
	}

	public AnnotationHandleGroup<T> build(Object adapter)
	{
		final var handles = methods.stream()
								   .map(m -> buildAnnotationHandle(adapter, m))
								   .toList();

		return new AnnotationHandleGroup<>(annotationClass, handles);
	}

	private IAdapterHandle.AnnotatedHandle<T> buildAnnotationHandle(final Object adapter,
																	final ExecutionMethod<T> method)
	{
		final var consumerHandleBuilder = method.executionHandleBuilder;
		final var annotation = method.method.annotation();
		final var annotationHandle = new IAdapterHandle.AnnotatedHandle<>(annotation,
																		  method.method.method(),
																		  consumerHandleBuilder.build(adapter));
		return annotationHandle;
	}

	public record ExecutionMethod<T extends Annotation>(ReflectUtils.AnnotatedMethod<T> method,
															   IExecutionHandleBuilder executionHandleBuilder)
	{}
}
