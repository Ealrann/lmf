package org.logoce.lmf.core.api.notification.listener;

@FunctionalInterface
public interface LongListener extends IModelListener
{
	void notify(long oldValue, long newValue);
}