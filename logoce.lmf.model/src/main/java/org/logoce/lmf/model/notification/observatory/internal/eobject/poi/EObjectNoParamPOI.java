package org.logoce.lmf.model.notification.observatory.internal.eobject.poi;

import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.LMObject;

import java.util.List;

public final class EObjectNoParamPOI implements IEObjectPOI
{
	private final Runnable listener;
	private final List<Feature<?, ?>> features;

	public EObjectNoParamPOI(final Runnable listener, final List<Feature<?, ?>> features)
	{
		this.listener = listener;
		this.features = List.copyOf(features);
	}

	@Override
	public void listen(final LMObject object)
	{
		object.listenNoParam(listener, features);
	}

	@Override
	public void sulk(final LMObject object)
	{
		object.sulkNoParam(listener, features);
	}
}
