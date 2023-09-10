package org.logoce.extender.impl.util;

import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.IAdapterDescriptorRegistry;
import org.logoce.extender.api.IAdapterExtension;
import org.logoce.extender.ext.IAdapterHandleFactory;
import org.logoce.extender.impl.AdapterDescriptor;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

public record DescriptorContextBuilder<T extends IAdapter>(AdapterDescriptor<T> descriptor)
{
	public static <T extends IAdapter> IAdapterDescriptorRegistry.DescriptorContext<T> build(final AdapterDescriptor<T> descriptor)
	{
		return new DescriptorContextBuilder<>(descriptor).build();
	}

	private static final List<IAdapterExtension.Descriptor> adapterExtensions = StreamSupport.stream(ServiceLoader.load(
																														  IAdapterExtension.Descriptor.class)
																												  .spliterator(),
																									 false)
																							 .toList();

	public IAdapterDescriptorRegistry.DescriptorContext<T> build()
	{
		return IAdapterHandleFactory.FACTORIES.stream()
											  .filter(this::isFactoryApplicable)
											  .map(this::newContext)
											  .findAny()
											  .orElseThrow(this::throwNoSecondaryAnnotation);
	}

	private IAdapterDescriptorRegistry.DescriptorContext<T> newContext(final IAdapterHandleFactory f)
	{
		final var handleBuilder = f.newBuilder(descriptor, adapterExtensions);
		return new IAdapterDescriptorRegistry.DescriptorContext<>(descriptor, handleBuilder);
	}

	private boolean isFactoryApplicable(final IAdapterHandleFactory f)
	{
		return descriptor.containsClassAnnotation(f.describedBy());
	}

	private MalformedParameterizedTypeException throwNoSecondaryAnnotation()
	{
		return new MalformedParameterizedTypeException("Model extender " + descriptor.extenderClass()
																					 .getSimpleName() + " should define one handle annotation (e.g: @Adapter, @Allocation)");
	}
}
