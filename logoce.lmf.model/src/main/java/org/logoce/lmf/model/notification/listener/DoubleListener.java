package org.logoce.lmf.model.notification.listener;

@FunctionalInterface
public interface DoubleListener extends IModelListener
{
	void notify(double oldValue, double newValue);
}