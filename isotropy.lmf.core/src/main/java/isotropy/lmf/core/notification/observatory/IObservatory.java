package isotropy.lmf.core.notification.observatory;

import org.sheepy.lily.core.api.model.ILilyEObject;

public interface IObservatory
{
	void observe(ILilyEObject source);
	void shut(ILilyEObject source);
}
