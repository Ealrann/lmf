package org.logoce.lmf.model.notification.util;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IEMFNotifier;
import org.logoce.lmf.model.api.notification.Notification;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public final class EMFListenerMap implements IEMFNotifier
{
	private final int featureCount;
	private final IndexFunction indexFunction;

	private Deque<Object>[] listenerMap = null;

	public EMFListenerMap(int featureCount, IndexFunction indexFunction)
	{
		this.featureCount = featureCount;
		this.indexFunction = indexFunction;
	}

	public void notify(Notification notification)
	{
		if (listenerMap != null)
		{
			final int featureId = notification.feature().featureSupplier().get().id();
			final int featureIdx = indexFunction.index(featureId);
			final var listeners = listenerMap[featureIdx];
			if (listeners != null)
			{
				notify(listeners, notification);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void notify(final Deque<Object> notificationListeners, final Notification notification)
	{
		for (final var listener : notificationListeners)
		{
			if (listener instanceof Runnable runnable)
			{
				runnable.run();
			}
			else
			{
				((Consumer<Notification>) listener).accept(notification);
			}
		}
	}

	@Override
	public void listen(Consumer<Notification> listener, List<RawFeature<?, ?>> features)
	{
		listenInternal(listener, features);
	}

	@Override
	public void listenNoParam(Runnable listener, List<RawFeature<?, ?>> features)
	{
		listenInternal(listener, features);
	}

	private void listenInternal(Object listener, List<RawFeature<?, ?>> features)
	{
		if (listenerMap == null)
		{
			initNotificationMap();
		}

		for (final var feature : features)
		{
			registerNotificationListener(listener, feature);
		}
	}

	@Override
	public void sulk(Consumer<Notification> listener, List<RawFeature<?, ?>> features)
	{
		sulkInternal(listener, features);
	}

	@Override
	public void sulkNoParam(Runnable listener, List<RawFeature<?, ?>> features)
	{
		sulkInternal(listener, features);
	}

	private void sulkInternal(Object listener, List<RawFeature<?, ?>> features)
	{
		if (listenerMap != null)
		{
			for (final var feature : features)
			{
				final int featureId = feature.featureSupplier().get().id();
				final int featureIdx = indexFunction.index(featureId);
				final var list = listenerMap[featureIdx];
				if (list != null)
				{
					list.remove(listener);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void initNotificationMap()
	{
		listenerMap = new Deque[featureCount];
	}

	private void registerNotificationListener(final Object listener, final RawFeature<?, ?> feature)
	{
		final int featureId = feature.featureSupplier().get().id();
		final int featureIdx = indexFunction.index(featureId);
		var list = listenerMap[featureIdx];
		if (list == null)
		{
			list = new ConcurrentLinkedDeque<>();
			listenerMap[featureIdx] = list;
		}
		list.add(listener);
	}

	@FunctionalInterface
	public interface IndexFunction
	{
		int index(int id);
	}
}
