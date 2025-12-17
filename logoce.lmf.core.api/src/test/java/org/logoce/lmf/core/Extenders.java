package org.logoce.lmf.core;

import org.logoce.lmf.core.api.extender.IAdapter;
import org.logoce.lmf.core.api.extender.IAdapterProvider;
import org.logoce.lmf.core.functional.ModelExplorerAdapters;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class Extenders implements IAdapterProvider
{
	@Override
	public List<Class<? extends IAdapter>> classifiers()
	{
		return List.of(ModelExplorerAdapters.CarInfoAdapter.class,
					   ModelExplorerAdapters.PersonInfoAdapter.class);
	}

	@Override
	public MethodHandles.Lookup lookup()
	{
		return MethodHandles.lookup();
	}
}

