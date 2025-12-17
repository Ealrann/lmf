package org.logoce.lmf.generator;

import org.logoce.lmf.core.api.extender.IAdapter;
import org.logoce.lmf.core.api.extender.IAdapterProvider;
import org.logoce.lmf.generator.adapter.*;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class Extenders implements IAdapterProvider
{
	public Extenders()
	{
	}

	@Override
	public List<Class<? extends IAdapter>> classifiers()
	{
		return List.of(FeatureResolution.class,
					   GroupInterfaceType.class,
					   GroupImplementationType.class,
					   GroupBuilderInterfaceType.class,
					   GroupBuilderClassType.class,
					   ModelResolution.class);
	}

	@Override
	public MethodHandles.Lookup lookup()
	{
		return MethodHandles.lookup();
	}
}
