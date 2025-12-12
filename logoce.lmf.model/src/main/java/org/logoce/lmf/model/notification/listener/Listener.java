package org.logoce.lmf.model.notification.listener;

@FunctionalInterface
public interface Listener<T> extends IModelListener
{
	void notify(T oldValue, T newValue);
}