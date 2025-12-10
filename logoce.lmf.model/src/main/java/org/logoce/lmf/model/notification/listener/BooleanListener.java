package org.logoce.lmf.model.notification.listener;

@FunctionalInterface
public interface BooleanListener
{
	void notify(boolean oldValue, boolean newValue);
}
