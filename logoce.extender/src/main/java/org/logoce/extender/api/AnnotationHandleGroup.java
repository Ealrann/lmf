package org.logoce.extender.api;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;

public record AnnotationHandleGroup<T extends Annotation>(Class<T> annotationClass, List<IAdapterHandle.AnnotatedHandle<T>> handles)
{
	public boolean match(Class<? extends Annotation> classifier)
	{
		return annotationClass == classifier;
	}

	public Stream<IAdapterHandle.AnnotatedHandle<T>> stream()
	{
		return handles.stream();
	}
}
