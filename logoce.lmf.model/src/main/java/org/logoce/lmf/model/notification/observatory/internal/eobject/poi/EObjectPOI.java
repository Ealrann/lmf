package org.logoce.lmf.model.notification.observatory.internal.eobject.poi;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.LMObject;

import java.util.List;
import java.util.function.Consumer;

public final class EObjectPOI implements IEObjectPOI
{
	private final Consumer<Notification> listener;
	private final List<Feature<?, ?>> features;

	public EObjectPOI(final Consumer<Notification> listener, final List<Feature<?, ?>> features)
	{
		this.listener = listener;
		this.features = List.copyOf(features);
	}

	@Override
	public void listen(final LMObject object)
	{
		object.listen(listener, features);
	}

	@Override
	public void sulk(final LMObject object)
	{
		object.sulk(listener, features);
	}
}
