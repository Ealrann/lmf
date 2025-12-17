package org.logoce.lmf.core.observatory.allocation;

import org.logoce.lmf.core.api.extender.IAdapter;
import org.logoce.lmf.core.api.extender.IAdapterHandle;

public final class AdapterPOI<T extends IAdapter> implements IAdapterPOI<T>
{
	private final IAdapterHandle.ExtenderListener<T> onNewExtender;

	public AdapterPOI(final IAdapterHandle.ExtenderListener<T> onNewExtender)
	{
		this.onNewExtender = onNewExtender;
	}

	@Override
	public void listen(final IAdapterHandle<T> handle)
	{
		handle.listen(onNewExtender);
	}

	@Override
	public void sulk(final IAdapterHandle<T> handle)
	{
		handle.sulk(onNewExtender);
	}
}
