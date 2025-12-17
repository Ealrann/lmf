package org.logoce.lmf.core.api.extender.ext;

import org.logoce.lmf.core.api.extender.IAdaptable;

public interface IAdaptableNameMatcher
{
	boolean match(IAdaptable adaptable, String name);
}
