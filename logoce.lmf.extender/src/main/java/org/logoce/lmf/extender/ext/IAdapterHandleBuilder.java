package org.logoce.lmf.extender.ext;

import org.logoce.lmf.extender.api.IAdaptable;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.extender.api.IAdapterHandle;

public interface IAdapterHandleBuilder<E extends IAdapter>
{
	IAdapterHandle<E> build(IAdaptable target);
	Class<? extends IAdapterHandle<E>> getHandleClass();
}
