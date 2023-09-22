package org.logoce.lmf.extender.ext;

import org.logoce.lmf.extender.api.IAdaptable;

public interface IAdaptableNameMatcher
{
	boolean match(IAdaptable adaptable, String name);
}
