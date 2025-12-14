package org.logoce.lmf.core.notification.listener;

@FunctionalInterface
public interface BooleanListener extends IModelListener
{
	void notify(boolean oldValue, boolean newValue);
}
