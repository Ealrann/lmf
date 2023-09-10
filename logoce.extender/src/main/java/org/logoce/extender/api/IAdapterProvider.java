package org.logoce.extender.api;

import java.lang.invoke.MethodHandles;
import java.util.List;

public interface IAdapterProvider
{
	List<Class<? extends IAdapter>> classifiers();
	MethodHandles.Lookup lookup();
}
