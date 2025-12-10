package org.logoce.lmf.model.notification.listener;

@FunctionalInterface
public interface LongListener
{
	void notify(long oldValue, long newValue);
}