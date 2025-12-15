package org.logoce.lmf.core.api.notification.listener;

@FunctionalInterface
public interface DoubleListener extends IModelListener
{
	void notify(double oldValue, double newValue);
}