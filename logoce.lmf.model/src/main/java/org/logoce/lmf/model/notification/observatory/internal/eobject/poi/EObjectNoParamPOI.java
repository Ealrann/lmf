package org.logoce.lmf.model.notification.observatory.internal.eobject.poi;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.lang.LMObject;

import java.util.List;

public final class EObjectNoParamPOI implements IEObjectPOI
{
	private final Runnable listener;
	private final List<RawFeature<?, ?>> features;

	public EObjectNoParamPOI(Runnable listener, List<RawFeature<?, ?>> features)
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
