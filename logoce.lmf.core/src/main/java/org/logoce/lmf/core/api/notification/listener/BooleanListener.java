package org.logoce.lmf.core.api.notification.listener;

@FunctionalInterface
public interface BooleanListener extends IModelListener
{
	void notify(boolean oldValue, boolean newValue);
}
