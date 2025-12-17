package org.logoce.lmf.core.api.extender.parameter;

import org.logoce.lmf.core.api.extender.IAdaptable;

import java.lang.annotation.Annotation;

public interface IParameterResolver
{
	boolean isApplicable(final Class<?> parameterClass, final Annotation parameterAnnotation);
	Object resolve(final IAdaptable target, final Class<?> parameterClass);
}
