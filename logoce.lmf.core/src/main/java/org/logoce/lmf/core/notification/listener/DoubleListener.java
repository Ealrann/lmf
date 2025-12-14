package org.logoce.lmf.core.notification.listener;

@FunctionalInterface
public interface DoubleListener extends IModelListener
{
	void notify(double oldValue, double newValue);
}