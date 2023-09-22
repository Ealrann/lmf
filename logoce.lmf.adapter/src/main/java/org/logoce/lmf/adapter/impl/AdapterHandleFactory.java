package org.logoce.lmf.adapter.impl;

import org.logoce.lmf.adapter.api.Adapter;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.extender.api.IAdapterDescriptor;
import org.logoce.lmf.extender.api.IAdapterExtension;
import org.logoce.lmf.extender.ext.IAdapterHandleBuilder;
import org.logoce.lmf.extender.ext.IAdapterHandleFactory;

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
