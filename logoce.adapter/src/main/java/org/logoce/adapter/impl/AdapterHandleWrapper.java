package org.logoce.adapter.impl;

import org.logoce.extender.api.IAdaptable;
import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.IAdapterHandle;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

public final class AdapterHandleWrapper<Extender extends IAdapter> implements IAdapterHandle<Extender>
{
	private final Extender extender;

	public AdapterHandleWrapper(final Extender extender)
	{
		this.extender = extender;
	}

	@Override
	public void load(IAdaptable target)
	{
	}

	@Override
	public void dispose(IAdaptable target)
	{
	}

	@Override
	public <A extends Annotation> Stream<AnnotatedHandle<A>> annotatedHandles(Class<A> annotationClass)
	{
		return Stream.empty();
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
