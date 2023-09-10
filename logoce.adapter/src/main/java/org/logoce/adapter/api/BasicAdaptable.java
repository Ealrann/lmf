package org.logoce.adapter.api;

import org.logoce.extender.api.IAdaptable;
import org.logoce.extender.api.IAdapterManager;

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
