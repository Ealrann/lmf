package logoce.lmf.model.notification.observatory;

import logoce.lmf.model.lang.LMObject;

public interface IObservatory
{
	void observe(LMObject source);
	void shut(LMObject source);
}
