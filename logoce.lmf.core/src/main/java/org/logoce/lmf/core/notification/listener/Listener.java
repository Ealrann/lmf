package org.logoce.lmf.core.notification.listener;

@FunctionalInterface
public interface Listener<T> extends IModelListener
{
	void notify(T oldValue, T newValue);
}