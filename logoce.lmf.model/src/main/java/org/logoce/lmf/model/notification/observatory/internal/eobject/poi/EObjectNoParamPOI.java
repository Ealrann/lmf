package org.logoce.lmf.model.notification.observatory.internal.eobject.poi;

import org.logoce.lmf.model.lang.LMObject;

public final class EObjectNoParamPOI implements IEObjectPOI
{
	private final Runnable listener;
	private final int[] features;

	public EObjectNoParamPOI(final Runnable listener, final int... features)
	{
		this.listener = listener;
		this.features = features.clone();
	}

	@Override
	public void listen(final LMObject object)
	{
		object.notifier().listenNoParam(listener, features);
	}

	@Override
	public void sulk(final LMObject object)
	{
		object.notifier().sulkNoParam(listener, features);
	}
}
