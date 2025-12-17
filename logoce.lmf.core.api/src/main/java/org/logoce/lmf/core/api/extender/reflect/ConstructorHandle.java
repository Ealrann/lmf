package org.logoce.lmf.core.api.extender.reflect;

import java.lang.reflect.Constructor;

public interface ConstructorHandle<T>
{
	T newInstance(Object... parameters);
	Constructor<T> getJavaConstructor();
}
