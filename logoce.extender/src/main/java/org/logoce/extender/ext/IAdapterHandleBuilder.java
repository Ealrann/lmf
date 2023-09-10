package org.logoce.extender.ext;

import org.logoce.extender.api.IAdaptable;
import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.IAdapterHandle;

public interface IAdapterHandleBuilder<E extends IAdapter>
{
	IAdapterHandle<E> build(IAdaptable target);
	Class<? extends IAdapterHandle<E>> getHandleClass();
}
