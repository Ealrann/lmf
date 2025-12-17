package org.logoce.lmf.core.adapter;

import org.logoce.lmf.core.api.adapter.Adapter;
import org.logoce.lmf.core.api.extender.IAdapter;
import org.logoce.lmf.core.api.extender.IAdapterDescriptor;
import org.logoce.lmf.core.api.extender.IAdapterExtension;
import org.logoce.lmf.core.api.extender.ext.IAdapterHandleBuilder;
import org.logoce.lmf.core.api.extender.ext.IAdapterHandleFactory;

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
