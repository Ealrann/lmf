package org.logoce.adapter.api;

import org.logoce.extender.api.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class BasicAdapterManager implements IAdapterManager
{
	private final IAdaptable target;
	private final List<HandleWrapper<? extends IAdapter>> handles;

	private boolean disposed = false;

	public BasicAdapterManager(final IAdaptable target)
	{
		this.target = target;

		handles = IAdapterDescriptorRegistry.INSTANCE.descriptors(target)
													 .<HandleWrapper<?>>map(HandleWrapper::new)
													 .toList();
	}

	public void load()
	{
		if (disposed)
		{
			disposed = false;
			handles.forEach(w -> w.load(target));
		}
	}

	public void dispose()
	{
		if (!disposed)
		{
			handles.forEach(w -> w.dispose(target));
			disposed = true;
		}
	}

	@Override
	public <T extends IAdapter> T adapt(final Class<T> type)
	{
		return this.<T>handles(filter(type))
				   .map(IAdapterHandle::getExtender)
				   .filter(Objects::nonNull)
				   .findAny()
				   .orElse(null);
	}

	@Override
	public <T extends IAdapter> T adapt(final Class<T> type, final String identifier)
	{
		return this.<T>handles(filter(type, identifier))
				   .map(IAdapterHandle::getExtender)
				   .filter(Objects::nonNull)
				   .findAny()
				   .orElse(null);
	}

	@Override
	public Stream<IAdapterDescriptor<?>> availableDescriptors()
	{
		return handles.stream()
					  .map(HandleWrapper::descriptor);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IAdapter> IAdapterHandle<T> adaptHandle(final IAdapterDescriptor<T> descriptor)
	{
		return handles.stream()
					  .filter(wrapper -> wrapper.descriptorContext.descriptor() == descriptor)
					  .map(handleWrapper -> handleWrapper.handle(target))
					  .map(handle -> (IAdapterHandle<T>) handle)
					  .findAny()
					  .orElse(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IAdapterHandle<?>> Stream<T> adaptHandlesOfType(final Class<T> handleType)
	{
		return handles.stream()
					  .filter(w -> handleType.isAssignableFrom(w.descriptorContext.handleBuilder()
																				  .getHandleClass()))
					  .map(handleWrapper -> handleWrapper.handle(target))
					  .map(handle -> (T) handle);
	}

	private static Predicate<HandleWrapper<?>> filter(final Class<? extends IAdapter> type, final String identifier)
	{
		return wrapper -> wrapper.descriptorContext.descriptor()
												   .match(type, identifier);
	}

	private static Predicate<HandleWrapper<?>> filter(final Class<? extends IAdapter> type)
	{
		return wrapper -> wrapper.descriptorContext.descriptor()
												   .match(type);
	}

	@SuppressWarnings("unchecked")
	private <T extends IAdapter> Stream<? extends IAdapterHandle<T>> handles(Predicate<HandleWrapper<?>> match)
	{
		return handles.stream()
					  .filter(match)
					  .map(handleWrapper -> handleWrapper.handle(target))
					  .map(handle -> (IAdapterHandle<T>) handle);
	}

	private static final class HandleWrapper<T extends IAdapter>
	{
		private final IAdapterDescriptorRegistry.DescriptorContext<T> descriptorContext;
		private IAdapterHandle<T> handle = null;

		public HandleWrapper(final IAdapterDescriptorRegistry.DescriptorContext<T> descriptorContext)
		{
			this.descriptorContext = descriptorContext;
		}

		public IAdapterHandle<T> handle(final IAdaptable target)
		{
			if (handle == null)
			{
				handle = descriptorContext.handleBuilder()
										  .build(target);
				handle.load(target);
			}
			return handle;
		}

		public IAdapterDescriptor<T> descriptor()
		{
			return descriptorContext.descriptor();
		}

		public void load(final IAdaptable target)
		{
			if (handle != null) handle.load(target);
		}

		public void dispose(final IAdaptable target)
		{
			if (handle != null) handle.dispose(target);
		}
	}
}
