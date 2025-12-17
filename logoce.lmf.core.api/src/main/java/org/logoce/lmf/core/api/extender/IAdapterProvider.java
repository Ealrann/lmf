package org.logoce.lmf.core.api.extender;

import java.lang.invoke.MethodHandles;
import java.util.List;

public interface IAdapterProvider
{
	List<Class<? extends IAdapter>> classifiers();
	MethodHandles.Lookup lookup();
}
