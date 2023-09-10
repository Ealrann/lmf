package org.logoce.extender.api;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;

public class AnnotationHandles
{
	private final List<AnnotationHandleGroup<?>> annotationHandles;

	public AnnotationHandles(final List<? extends AnnotationHandleGroup<?>> annotationHandles)
	{
		this.annotationHandles = List.copyOf(annotationHandles);
	}

	@SuppressWarnings("unchecked")
	public <A extends Annotation> Stream<IAdapterHandle.AnnotatedHandle<A>> stream(Class<A> annotationClass)
	{
		return annotationHandles.stream()
								.filter(h -> h.annotationClass()
											  .equals(annotationClass))
								.flatMap(h -> ((AnnotationHandleGroup<A>) h).handles()
																			.stream());
	}

	public boolean isEmpty()
	{
		return annotationHandles.isEmpty();
	}
}
