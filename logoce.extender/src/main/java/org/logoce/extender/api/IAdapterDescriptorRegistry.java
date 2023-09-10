package org.logoce.extender.api;

import org.logoce.extender.ext.IAdapterHandleBuilder;

import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface IAdapterDescriptorRegistry
{
	Stream<IAdapterDescriptor<?>> streamDescriptors();
	Stream<DescriptorContext<? extends IAdapter>> descriptors(final IAdaptable target);
	<T extends IAdapter> Stream<IAdapterDescriptor<T>> streamDescriptors(final IAdaptable target, final Class<T> type);

	IAdapterDescriptorRegistry INSTANCE = ServiceLoader.load(IAdapterDescriptorRegistry.class)
													   .findFirst()
													   .orElse(null);

	record DescriptorContext<T extends IAdapter>(IAdapterDescriptor<T> descriptor,
												 IAdapterHandleBuilder<T> handleBuilder)
	{
		public IAdapterHandle<T> newHandle(IAdaptable target)
		{
			return handleBuilder.build(target);
		}
	}
}
