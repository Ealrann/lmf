package org.logoce.lmf.extender.impl;

import org.logoce.lmf.extender.api.*;
import org.logoce.lmf.extender.impl.util.DescriptorContextBuilder;
import org.logoce.lmf.extender.impl.util.ExtenderDescriptorBuilder;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Stream;

public final class AdapterDescriptorRegistry implements IAdapterDescriptorRegistry
{
	private final List<DescriptorContext<?>> descriptors;

	public AdapterDescriptorRegistry()
	{
		final var mapBuilder = new ExtenderMapBuilder();

		ServiceLoader.load(IAdapterProvider.class)
					 .stream()
					 .map(ServiceLoader.Provider::get)
					 .forEach(mapBuilder::append);

		final var extenderMap = mapBuilder.build();
		final var descriptorBuilder = new ExtenderDescriptorBuilder(extenderMap.lookupMap);

		descriptors = extenderMap.extenderClasses.stream()
												 .map(descriptorBuilder::build)
												 .flatMap(Optional::stream)
												 .<DescriptorContext<?>>map(DescriptorContextBuilder::build)
												 .toList();
	}

	@Override
	public Stream<IAdapterDescriptor<?>> streamDescriptors()
	{
		return descriptors.stream().map(DescriptorContext::descriptor);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IAdapter> Stream<IAdapterDescriptor<T>> streamDescriptors(final IAdaptable target,
																				final Class<T> type)
	{
		return descriptors.stream()
						  .filter(descriptor -> descriptor.descriptor().isApplicable(target))
						  .filter(descriptor -> descriptor.descriptor().match(type))
						  .map(descriptor -> ((DescriptorContext<T>) descriptor))
						  .map(DescriptorContext::descriptor);
	}

	@Override
	public Stream<DescriptorContext<?>> descriptors(final IAdaptable target)
	{
		return descriptors.stream().filter(descriptor -> descriptor.descriptor().isApplicable(target));
	}

	public record ExtenderMap(Map<Module, MethodHandles.Lookup> lookupMap,
							  List<Class<? extends IAdapter>> extenderClasses) {}

	private static final class ExtenderMapBuilder
	{
		private final Map<Module, MethodHandles.Lookup> lookupMap = new HashMap<>();
		private final Set<Class<? extends IAdapter>> extenderClasses = new HashSet<>();

		public void append(IAdapterProvider provider)
		{
			final var module = provider.getClass().getModule();
			lookupMap.computeIfAbsent(module, _ -> provider.lookup());
			extenderClasses.addAll(provider.classifiers());
		}

		public ExtenderMap build()
		{
			return new ExtenderMap(Map.copyOf(lookupMap), List.copyOf(extenderClasses));
		}
	}
}
