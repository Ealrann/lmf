package org.logoce.lmf.core.api.model;

public interface IJavaWrapperConverter<T>
{
	T create(String it);

	String convert(T it);
}

