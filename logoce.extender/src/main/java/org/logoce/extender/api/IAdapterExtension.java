package org.logoce.extender.api;

import org.logoce.extender.api.parameter.IParameterResolver;

import java.util.Optional;

public interface IAdapterExtension
{
	void load(final IAdaptable target);
	void dispose(final IAdaptable target);
	boolean isEmpty();

	@FunctionalInterface
	interface Descriptor
	{
		Builder newBuilder();
	}

	interface Builder
	{
		IAdapterExtension build(IAdapterDescriptor.ExtenderContext<?> context);
		Optional<IParameterResolver> parameterResolver();
	}
}
