package org.logoce.lmf.model.notification.listener;

@FunctionalInterface
public interface FloatListener
{
	void notify(float oldValue, float newValue);
}