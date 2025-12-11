package org.logoce.lmf.model.notification.util;

import org.logoce.lmf.model.api.model.IModelNotifier;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.Feature;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public final class ModelListenerMap implements IModelNotifier
{
	private final int featureCount;
	private final IndexFunction indexFunction;

	private Deque<Object>[] listenerMap = null;

	public ModelListenerMap(int featureCount, IndexFunction indexFunction)
	{
		this.featureCount = featureCount;
		this.indexFunction = indexFunction;
	}

	public void notify(Notification notification)
	{
		if (listenerMap != null)
		{
			final int featureId = notification.featureId();
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
	public void listen(Consumer<Notification> listener, List<Feature<?, ?, ?, ?>> features)
	{
		listenInternal(listener, features.stream().mapToInt(this::featureToIndex));
	}

	@Override
	public void listen(final Consumer<Notification> listener, final int... featureIDs)
	{
		listenInternal(listener, IntStream.of(featureIDs).map(this::IDtoIndex));
	}

	@Override
	public void listenNoParam(Runnable listener, List<Feature<?, ?, ?, ?>> features)
	{
		listenInternal(listener, features.stream().mapToInt(this::featureToIndex));
	}

	@Override
	public void listenNoParam(final Runnable listener, final int... featureIDs)
	{
		listenInternal(listener, IntStream.of(featureIDs).map(this::IDtoIndex));
	}

	private void listenInternal(Object listener, IntStream featureIds)
	{
		if (listenerMap == null)
		{
			initNotificationMap();
		}

		featureIds.forEach(f -> registerNotificationListener(listener, f));
	}

	@Override
	public void sulk(Consumer<Notification> listener, List<Feature<?, ?, ?, ?>> features)
	{
		sulkInternal(listener, features.stream().mapToInt(this::featureToIndex));
	}

	@Override
	public void sulk(final Consumer<Notification> listener, final int... featureIDs)
	{
		sulkInternal(listener, IntStream.of(featureIDs).map(this::IDtoIndex));
	}

	@Override
	public void sulkNoParam(Runnable listener, List<Feature<?, ?, ?, ?>> features)
	{
		sulkInternal(listener, features.stream().mapToInt(this::featureToIndex));
	}

	@Override
	public void sulkNoParam(final Runnable listener, final int... featureIDs)
	{
		sulkInternal(listener, IntStream.of(featureIDs).map(this::IDtoIndex));
	}

	private void sulkInternal(Object listener, IntStream featureIds)
	{
		if (listenerMap != null)
		{
			featureIds.forEach(f -> unregisterNotificationListener(listener, f));
		}
	}

	private void unregisterNotificationListener(final Object listener, final int featureIdx)
	{
		final var list = listenerMap[featureIdx];
		if (list != null)
		{
			list.remove(listener);
		}
	}

	private int featureToIndex(Feature<?, ?, ?, ?> feature)
	{
		final int featureId = feature.id();
		return IDtoIndex(featureId);
	}

	private int IDtoIndex(int id)
	{
		return indexFunction.index(id);
	}

	@SuppressWarnings("unchecked")
	private void initNotificationMap()
	{
		listenerMap = new Deque[featureCount];
	}

	private void registerNotificationListener(final Object listener, final int featureIdx)
	{
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
