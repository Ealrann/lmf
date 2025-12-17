package org.logoce.lmf.core.api.extender;

import org.logoce.lmf.core.api.extender.parameter.IParameterResolver;

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
