package org.logoce.lmf.model.notification.listener;

@FunctionalInterface
public interface LongListener extends IModelListener
{
	void notify(long oldValue, long newValue);
}