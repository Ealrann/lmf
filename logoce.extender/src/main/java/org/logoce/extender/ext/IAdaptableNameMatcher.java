package org.logoce.extender.ext;

import org.logoce.extender.api.IAdaptable;

public interface IAdaptableNameMatcher
{
	boolean match(IAdaptable adaptable, String name);
}
