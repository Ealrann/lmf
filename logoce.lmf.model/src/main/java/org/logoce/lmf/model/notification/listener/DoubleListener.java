package org.logoce.lmf.model.notification.listener;

@FunctionalInterface
public interface DoubleListener
{
	void notify(double oldValue, double newValue);
}