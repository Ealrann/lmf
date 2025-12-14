package org.logoce.lmf.core.notification.listener;

@FunctionalInterface
public interface IntListener extends IModelListener
{
	void notify(int oldValue, int newValue);
}