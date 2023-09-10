package org.logoce.adapter.impl;

import org.logoce.extender.api.*;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;

public final class AdapterHandleFull<Extender extends IAdapter> implements IAdapterHandle<Extender>
{
	private final Extender extender;
	private final AnnotationHandles annotationHandles;
	private final List<IAdapterExtension> extensions;

	public AdapterHandleFull(final Extender extender,
							 final AnnotationHandles annotationHandles,
							 final List<IAdapterExtension> extensions)
	{
		this.extender = extender;
		this.annotationHandles = annotationHandles;
		this.extensions = List.copyOf(extensions);
	}

	@Override
	public void load(final IAdaptable target)
	{
		for (final var extension : extensions)
		{
			try
			{
				extension.load(target);
			}
			catch (Throwable e)
			{
				throwObserveError(e, extension);
			}
		}
	}

	@Override
	public void dispose(final IAdaptable target)
	{
		extensions.forEach(e -> e.dispose(target));
	}

	@Override
	public <A extends Annotation> Stream<IAdapterHandle.AnnotatedHandle<A>> annotatedHandles(Class<A> annotationClass)
	{
		return annotationHandles.stream(annotationClass);
	}

	private void throwObserveError(final Throwable e, final IAdapterExtension extension)
	{
		final var extenderName = extender.getClass()
										 .getSimpleName();
		final var error = new AssertionError("Failed to start " + extension.getClass()
																		   .getSimpleName() + " for " + extenderName,
											 e);
		error.printStackTrace();
	}

	@Override
	public Extender getExtender()
	{
		return extender;
	}

	@Override
	public void listen(final ExtenderListener<Extender> listener)
	{
		// Not changeable, nothing to listen
	}

	@Override
	public void listenNoParam(final Runnable listener)
	{
		// Not changeable, nothing to listen
	}

	@Override
	public void sulk(final ExtenderListener<Extender> listener)
	{
		// Not changeable, nothing to listen
	}

	@Override
	public void sulkNoParam(final Runnable listener)
	{
		// Not changeable, nothing to listen
	}
}
