package org.logoce.lmf.core.notification.listener;

@FunctionalInterface
public interface FloatListener extends IModelListener
{
	void notify(float oldValue, float newValue);
}