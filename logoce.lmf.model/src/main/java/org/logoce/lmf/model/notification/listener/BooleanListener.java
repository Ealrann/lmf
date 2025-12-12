package org.logoce.lmf.model.notification.listener;

@FunctionalInterface
public interface BooleanListener extends IModelListener
{
	void notify(boolean oldValue, boolean newValue);
}
