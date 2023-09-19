package isotropy.lmf.core.notification.observatory;

import isotropy.lmf.core.lang.LMObject;

public interface IObservatory
{
	void observe(LMObject source);
	void shut(LMObject source);
}
