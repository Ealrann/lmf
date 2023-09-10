package org.logoce.extender.ext;

import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.IAdapterDescriptor;
import org.logoce.extender.api.IAdapterExtension;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

public interface IAdapterHandleFactory
{
	Class<? extends Annotation> describedBy();
	<E extends IAdapter> IAdapterHandleBuilder<E> newBuilder(IAdapterDescriptor<E> descriptor,
															 List<IAdapterExtension.Descriptor> extensionDescriptors);

	List<IAdapterHandleFactory> FACTORIES = StreamSupport.stream(ServiceLoader.load(IAdapterHandleFactory.class)
																			  .spliterator(), false)
														 .toList();
}
