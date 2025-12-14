package org.logoce.lmf.core.notification.observatory;

import org.logoce.lmf.core.lang.LMObject;

public interface IObservatory
{
	void observe(LMObject source);
	void shut(LMObject source);
}
