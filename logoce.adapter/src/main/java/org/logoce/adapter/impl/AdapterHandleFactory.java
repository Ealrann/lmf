package org.logoce.adapter.impl;

import org.logoce.adapter.api.Adapter;
import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.IAdapterDescriptor;
import org.logoce.extender.api.IAdapterExtension;
import org.logoce.extender.ext.IAdapterHandleBuilder;
import org.logoce.extender.ext.IAdapterHandleFactory;

import java.lang.annotation.Annotation;
import java.util.List;

public final class AdapterHandleFactory implements IAdapterHandleFactory
{
	@Override
	public <E extends IAdapter> IAdapterHandleBuilder<E> newBuilder(final IAdapterDescriptor<E> descriptor,
																	final List<IAdapterExtension.Descriptor> extensionDescriptors)
	{
		return new AdapterHandleBuilder<>(descriptor, extensionDescriptors);
	}

	@Override
	public Class<? extends Annotation> describedBy()
	{
		return Adapter.class;
	}
}
