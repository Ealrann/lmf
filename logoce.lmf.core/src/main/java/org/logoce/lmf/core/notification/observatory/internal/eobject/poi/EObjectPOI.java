package org.logoce.lmf.core.notification.observatory.internal.eobject.poi;

import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.api.notification.Notification;

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
		object.notifier().listen(listener, features);
	}

	@Override
	public void sulk(final LMObject object)
	{
		object.notifier().sulk(listener, features);
	}
}
