package org.logoce.lmf.core.api.adapter;

import org.logoce.lmf.core.api.extender.IAdaptable;
import org.logoce.lmf.core.api.extender.IAdapterManager;

public abstract class BasicAdaptable implements IAdaptable
{
	private BasicAdapterManager adapterManager = null;

	protected BasicAdaptable()
	{
	}

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
