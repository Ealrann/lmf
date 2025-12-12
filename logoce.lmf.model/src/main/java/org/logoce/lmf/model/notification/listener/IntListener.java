package org.logoce.lmf.model.notification.listener;

@FunctionalInterface
public interface IntListener extends IModelListener
{
	void notify(int oldValue, int newValue);
}