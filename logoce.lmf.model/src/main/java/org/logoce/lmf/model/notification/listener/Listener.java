package org.logoce.lmf.model.notification.listener;

@FunctionalInterface
public interface Listener<T>
{
	void notify(T oldValue, T newValue);
}