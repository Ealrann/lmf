package org.logoce.lmf.model.notification.observatory.internal.eobject.poi;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.api.notification.Notification;

import java.util.function.Consumer;

public final class EObjectPOI implements IEObjectPOI
{
	private final Consumer<Notification> listener;
	private final int[] features;

	public EObjectPOI(final Consumer<Notification> listener, final int... features)
	{
		this.listener = listener;
		this.features = features.clone();
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
