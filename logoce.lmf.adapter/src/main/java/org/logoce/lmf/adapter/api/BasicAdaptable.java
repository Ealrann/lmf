package org.logoce.lmf.adapter.api;

import org.logoce.lmf.extender.api.IAdaptable;
import org.logoce.lmf.extender.api.IAdapterManager;

public abstract class BasicAdaptable implements IAdaptable
{
	private BasicAdapterManager adapterManager = null;

	@Override
	public IAdapterManager adapterManager()
	{
		if (adapterManager == null)
		{
			adapterManager = new BasicAdapterManager(this);
		}
		return adapterManager;
	}
}
