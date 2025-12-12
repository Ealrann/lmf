package org.logoce.lmf.model.notification.listener;

@FunctionalInterface
public interface FloatListener extends IModelListener
{
	void notify(float oldValue, float newValue);
}