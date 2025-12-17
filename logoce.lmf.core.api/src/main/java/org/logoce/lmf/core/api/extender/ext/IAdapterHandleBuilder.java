package org.logoce.lmf.core.api.extender.ext;

import org.logoce.lmf.core.api.extender.IAdaptable;
import org.logoce.lmf.core.api.extender.IAdapter;
import org.logoce.lmf.core.api.extender.IAdapterHandle;

public interface IAdapterHandleBuilder<E extends IAdapter>
{
	IAdapterHandle<E> build(IAdaptable target);
	Class<? extends IAdapterHandle<E>> getHandleClass();
}
