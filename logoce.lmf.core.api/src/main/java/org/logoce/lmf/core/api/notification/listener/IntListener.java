package org.logoce.lmf.core.api.notification.listener;

@FunctionalInterface
public interface IntListener extends IModelListener
{
	void notify(int oldValue, int newValue);
}