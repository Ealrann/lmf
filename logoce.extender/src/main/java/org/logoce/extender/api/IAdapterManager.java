package org.logoce.extender.api;

import java.util.stream.Stream;

public interface IAdapterManager
{
	<T extends IAdapter> T adapt(Class<T> type);
	<T extends IAdapter> T adapt(Class<T> type, String identifier);

	Stream<IAdapterDescriptor<?>> availableDescriptors();
	<T extends IAdapter> IAdapterHandle<T> adaptHandle(IAdapterDescriptor<T> descriptor);
	<T extends IAdapterHandle<?>> Stream<T> adaptHandlesOfType(final Class<T> handleType);

	@SuppressWarnings("unchecked")
	default <T extends IAdapter> Stream<IAdapterDescriptor<T>> availableDescriptors(Class<T> type)
	{
		return availableDescriptors().filter(descriptor -> descriptor.match(type))
									 .map(descriptor -> (IAdapterDescriptor<T>) descriptor);
	}

	default <T extends IAdapter> Stream<IAdapterHandle<T>> adaptHandles(Class<T> type)
	{
		return availableDescriptors(type).map(this::adaptHandle);
	}
}
