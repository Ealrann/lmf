package org.logoce.lmf.core.observatory.allocation;

import org.logoce.lmf.core.api.extender.IAdapter;
import org.logoce.lmf.core.api.extender.IAdapterHandle;

public interface IAdapterPOI<T extends IAdapter>
{
	void listen(IAdapterHandle<T> handle);
	void sulk(IAdapterHandle<T> handle);
}
