package org.logoce.lmf.model.notification.observatory;

import org.logoce.lmf.model.lang.LMObject;

public interface IObservatory
{
	void observe(LMObject source);
	void shut(LMObject source);
}
