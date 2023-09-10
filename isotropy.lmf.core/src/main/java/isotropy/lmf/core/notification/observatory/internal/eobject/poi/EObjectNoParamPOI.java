package isotropy.lmf.core.notification.observatory.internal.eobject.poi;

import org.sheepy.lily.core.api.model.ILilyEObject;

public final class EObjectNoParamPOI implements IEObjectPOI
{
	private final Runnable listener;
	private final int[] features;

	public EObjectNoParamPOI(Runnable listener, int... features)
	{
		this.listener = listener;
		this.features = features;
	}

	@Override
	public void listen(final ILilyEObject object)
	{
		object.listenNoParam(listener, features);
	}

	@Override
	public void sulk(final ILilyEObject object)
	{
		object.sulkNoParam(listener, features);
	}
}
