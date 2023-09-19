package isotropy.lmf.core.notification.util;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.model.IEMFNotifier;
import isotropy.lmf.core.api.notification.Notification;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public final class EMFListenerMap implements IEMFNotifier
{
	private final int featureCount;

	private Map<RawFeature<?, ?>, Deque<Object>> listenerMap = null;

	public EMFListenerMap(int featureCount)
	{
		this.featureCount = featureCount;
	}

	public void notify(Notification notification)
	{
		if (listenerMap != null)
		{
			final var listeners = listenerMap.get(notification.feature());
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
				final var list = listenerMap.get(feature);
				if (list != null)
				{
					list.remove(listener);
				}
			}
		}
	}

	private void initNotificationMap()
	{
		listenerMap = new HashMap<>(featureCount);
	}

	private void registerNotificationListener(final Object listener, final RawFeature<?, ?> feature)
	{
		var list = listenerMap.get(feature);
		if (list == null)
		{
			list = new ConcurrentLinkedDeque<>();
			listenerMap.put(feature, list);
		}
		list.add(listener);
	}
}
