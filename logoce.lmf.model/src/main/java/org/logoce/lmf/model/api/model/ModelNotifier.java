package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.notification.util.ModelListenerMap;

import java.util.List;
import java.util.function.Consumer;

public abstract class LilyBasicNotifier implements IModelNotifier
{
	private final ModelListenerMap listenerMap;

	private boolean deliver;

	public LilyBasicNotifier()
	{
		final int featureCount = 100;
		listenerMap = new ModelListenerMap(featureCount, this::featureIndex);
	}

	abstract protected int featureIndex(int featureId);

	protected void eNotify(final Notification notification)
	{
		if (eDeliver()) listenerMap.notify(notification);
	}

	public boolean eDeliver()
	{
		return deliver;
	}

	public void eDeliver(boolean deliver)
	{
		this.deliver = deliver;
	}

	@Override
	public void listen(final Consumer<Notification> listener, final int... featureIDs)
	{
		listenerMap.listen(listener, featureIDs);
	}

	@Override
	public final void listen(Consumer<Notification> listener, List<Feature<?, ?, ?, ?>> features)
	{
		listenerMap.listen(listener, features);
	}

	@Override
	public void sulk(final Consumer<Notification> listener, final int... featureIDs)
	{
		listenerMap.sulk(listener, featureIDs);
	}

	@Override
	public final void sulk(Consumer<Notification> listener, List<Feature<?, ?, ?, ?>> features)
	{
		listenerMap.sulk(listener, features);
	}

	@Override
	public void listenNoParam(Runnable listener, final int... featureIDs)
	{
		listenerMap.listenNoParam(listener, featureIDs);
	}

	@Override
	public final void listenNoParam(Runnable listener, List<Feature<?, ?, ?, ?>> features)
	{
		listenerMap.listenNoParam(listener, features);
	}

	@Override
	public void sulkNoParam(final Runnable listener, final int... featureIDs)
	{
		listenerMap.sulkNoParam(listener, featureIDs);
	}

	@Override
	public final void sulkNoParam(Runnable listener, List<Feature<?, ?, ?, ?>> features)
	{
		listenerMap.sulkNoParam(listener, features);
	}

	public abstract Group<?> lmGroup();
}
