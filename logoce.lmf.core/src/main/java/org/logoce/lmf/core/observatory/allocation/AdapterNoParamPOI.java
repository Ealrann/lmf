package org.logoce.lmf.core.observatory.allocation;

import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.extender.api.IAdapterHandle;

public final class AdapterNoParamPOI<T extends IAdapter> implements IAdapterPOI<T>
{
	private final Runnable onNewExtender;

	public AdapterNoParamPOI(final Runnable onNewExtender)
	{
		this.onNewExtender = onNewExtender;
	}

	@Override
	public void listen(final IAdapterHandle<T> handle)
	{
		handle.listenNoParam(onNewExtender);
	}

	@Override
	public void sulk(final IAdapterHandle<T> handle)
	{
		handle.sulkNoParam(onNewExtender);
	}
}
