package org.logoce.lmf.core.notification.listener;

@FunctionalInterface
public interface LongListener extends IModelListener
{
	void notify(long oldValue, long newValue);
}