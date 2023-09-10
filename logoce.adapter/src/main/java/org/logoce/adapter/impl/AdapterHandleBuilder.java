package org.logoce.adapter.impl;

import org.logoce.adapter.api.Adapter;
import org.logoce.extender.api.*;
import org.logoce.extender.ext.IAdapterHandleBuilder;

import java.util.List;
import java.util.Optional;

public final class AdapterHandleBuilder<T extends IAdapter> implements IAdapterHandleBuilder<T>
{
	private final IAdapterDescriptor<T> extenderDescriptor;
	private final List<IAdapterExtension.Descriptor> extensionDescriptors;
	private final IAdapterHandle<T> singleton;

	public AdapterHandleBuilder(final IAdapterDescriptor<T> extenderDescriptor,
								final List<IAdapterExtension.Descriptor> extensionDescriptors)
	{
		this.extenderDescriptor = extenderDescriptor;
		this.extensionDescriptors = List.copyOf(extensionDescriptors);
		final var adapterAnnotation = extenderDescriptor.extenderClass()
														.getAnnotation(Adapter.class);
		final var isSingleton = adapterAnnotation.singleton();
		this.singleton = isSingleton ? newHandle(null) : null;
	}

	@Override
	public IAdapterHandle<T> build(final IAdaptable target)
	{
		return singleton != null ? singleton : newHandle(target);
	}

	private IAdapterHandle<T> newHandle(final IAdaptable target)
	{
		try
		{
			final var extensionBuilders = extensionDescriptors.stream()
															  .map(IAdapterExtension.Descriptor::newBuilder)
															  .toList();
			final var parameterResolvers = extensionBuilders.stream()
															.map(IAdapterExtension.Builder::parameterResolver)
															.filter(Optional::isPresent)
															.map(Optional::get);
			final var extenderContext = extenderDescriptor.newExtender(target, parameterResolvers);
			final var extensions = extensionBuilders.stream()
													.map(builder -> builder.build(extenderContext))
													.toList();
			return buildNewHandle(extenderContext, extensions);
		}
		catch (ReflectiveOperationException e)
		{
			throw new AssertionError(e);
		}
	}

	private IAdapterHandle<T> buildNewHandle(final IAdapterDescriptor.ExtenderContext<T> extenderContext,
											 final List<IAdapterExtension> extensions)
	{
		final var noAnnotations = extenderContext.annotationHandles()
												 .isEmpty();
		final var noExtensions = extensions.stream()
										   .allMatch(IAdapterExtension::isEmpty);
		if (noAnnotations && noExtensions)
		{
			return new AdapterHandleWrapper<>(extenderContext.extender());
		}
		else
		{
			return new AdapterHandleFull<>(extenderContext.extender(), extenderContext.annotationHandles(), extensions);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<AdapterHandleFull<T>> getHandleClass()
	{
		return (Class<AdapterHandleFull<T>>) (Class<?>) AdapterHandleFull.class;
	}
}
