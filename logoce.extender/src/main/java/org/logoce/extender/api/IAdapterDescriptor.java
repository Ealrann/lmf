package org.logoce.extender.api;

import org.logoce.extender.api.parameter.IParameterResolver;
import org.logoce.extender.api.reflect.ConsumerHandle;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

public interface IAdapterDescriptor<T extends IAdapter>
{
	Class<T> extenderClass();
	boolean match(Class<? extends IAdapter> classifier);
	boolean match(Class<? extends IAdapter> classifier, String identifier);
	boolean isApplicable(IAdaptable target);
	boolean containsMethodAnnotation(Class<? extends Annotation> annotationClass);
	boolean containsClassAnnotation(Class<? extends Annotation> annotationClass);
	<A extends Annotation> Stream<A> streamMethodAnnotations(Class<A> annotationClass);

	ExtenderContext<T> newExtender(IAdaptable target,
								   Stream<? extends IParameterResolver> resolvers) throws ReflectiveOperationException;

	default IAdapterHandle<T> adapHandle(final IAdaptable object)
	{
		return object.adapterManager()
					 .adaptHandle(this);
	}

	record ExtenderContext<T extends IAdapter>(T extender, AnnotationHandles annotationHandles)
	{
		public Stream<ConsumerHandle> annotatedConsumer(Class<? extends Annotation> annotationClass)
		{
			return annotationHandles.stream(annotationClass)
									.map(IAdapterHandle.AnnotatedHandle::executionHandle)
									.filter(ConsumerHandle.class::isInstance)
									.map(ConsumerHandle.class::cast);
		}
	}
}
