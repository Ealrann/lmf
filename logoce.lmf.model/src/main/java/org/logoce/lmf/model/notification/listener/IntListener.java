package org.logoce.lmf.model.notification.listener;

@FunctionalInterface
public interface IntListener
{
	void notify(int oldValue, int newValue);
}