package org.logoce.extender.api.parameter;

import org.logoce.extender.api.IAdaptable;

import java.lang.annotation.Annotation;

public interface IParameterResolver
{
	boolean isApplicable(final Class<?> parameterClass, final Annotation parameterAnnotation);
	Object resolve(final IAdaptable target, final Class<?> parameterClass);
}
