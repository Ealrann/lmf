package org.logoce.extender.api;

public interface IAdaptable
{
	IAdapterManager adapterManager();

	default <T extends IAdapter> T adapt(Class<T> type)
	{
		return adapterManager().adapt(type);
	}

	default <T extends IAdapter> T adapt(Class<T> type, String identifier)
	{
		return adapterManager().adapt(type, identifier);
	}
}
