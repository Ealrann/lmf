package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.notification.listener.BooleanListener;
import org.logoce.lmf.model.notification.listener.DoubleListener;
import org.logoce.lmf.model.notification.listener.FloatListener;
import org.logoce.lmf.model.notification.listener.IModelListener;
import org.logoce.lmf.model.notification.listener.IntListener;
import org.logoce.lmf.model.notification.listener.Listener;
import org.logoce.lmf.model.notification.listener.LongListener;
import org.logoce.lmf.notification.api.IFeatures;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public final class ModelNotifier<Type extends IFeatures<?>> implements IModelNotifier.Impl<Type>
{
	private final int featureCount;
	private final IndexFunction indexFunction;

	private Deque<Object>[] listenerMap = null;
	private Deque<Object> structureListeners = null;
	private boolean deliver;

	public ModelNotifier(int featureCount, IndexFunction indexFunction)
	{
		this.featureCount = featureCount;
		this.indexFunction = indexFunction;
	}

	@Override
	public boolean eDeliver()
	{
		return deliver;
	}

	@Override
	public void eDeliver(boolean deliver)
	{
		this.deliver = deliver;
	}

	@Override
	public void notify(Notification notification)
	{
		if (!eDeliver()) return;

		if (structureListeners != null && notification.isContainment())
		{
			dispatchStructureListeners(structureListeners, notification);
		}

		if (listenerMap != null)
		{
			final int featureId = notification.featureId();
			final int featureIdx = resolveFeatureIndex(featureId, notification);
			if (featureIdx < 0) return;

			final var listeners = listenerMap[featureIdx];
			if (listeners == null) return;

			final var group = notification.notifier().lmGroup();
			final Feature<?, ?, ?, ?> feature;
			try
			{
				feature = group.features().get(featureIdx);
			}
			catch (RuntimeException e)
			{
				dispatchListeners(listeners, notification, null);
				return;
			}

			dispatchListeners(listeners, notification, feature);
		}
	}

	private static void dispatchStructureListeners(final Deque<Object> notificationListeners,
												  final Notification notification)
	{
		for (final var listener : notificationListeners)
		{
			if (listener instanceof Runnable runnable)
			{
				runnable.run();
			}
			else
			{
				@SuppressWarnings("unchecked") final var notificationConsumer = (Consumer<Notification>) listener;
				notificationConsumer.accept(notification);
			}
		}
	}

	private static void dispatchListeners(final Deque<Object> listeners,
										 final Notification notification,
										 final Feature<?, ?, ?, ?> feature)
	{
		for (final var listener : listeners)
		{
			if (listener instanceof Runnable runnable)
			{
				runnable.run();
			}
			else if (listener instanceof Consumer<?> consumer)
			{
				@SuppressWarnings("unchecked") final var notificationConsumer = (Consumer<Notification>) consumer;
				notificationConsumer.accept(notification);
			}
			else if (listener instanceof IModelListener modelListener)
			{
				dispatchModelListener(modelListener, feature, notification);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void dispatchModelListener(final IModelListener listener,
											 final Feature<?, ?, ?, ?> feature,
											 final Notification notification)
	{
		if (feature == null) return;

		if (listener instanceof Listener<?> typedListener)
		{
			final var oldValue = normalizeValue(feature, notification.oldValue());
			final var newValue = normalizeValue(feature, notification.newValue());
			((Listener<Object>) typedListener).notify(oldValue, newValue);
			return;
		}

		if (feature.many()) return;

		final var oldValue = notification.oldValue();
		final var newValue = notification.newValue();

		if (listener instanceof BooleanListener booleanListener)
		{
			booleanListener.notify((Boolean) oldValue, (Boolean) newValue);
		}
		else if (listener instanceof IntListener intListener)
		{
			intListener.notify((Integer) oldValue, (Integer) newValue);
		}
		else if (listener instanceof LongListener longListener)
		{
			longListener.notify((Long) oldValue, (Long) newValue);
		}
		else if (listener instanceof FloatListener floatListener)
		{
			floatListener.notify((Float) oldValue, (Float) newValue);
		}
		else if (listener instanceof DoubleListener doubleListener)
		{
			doubleListener.notify((Double) oldValue, (Double) newValue);
		}
	}

	private static Object normalizeValue(final Feature<?, ?, ?, ?> feature, final Object value)
	{
		if (!feature.many())
		{
			return value;
		}

		if (value == null)
		{
			return List.of();
		}

		if (value instanceof List<?>)
		{
			return value;
		}

		return List.of(value);
	}

	private int resolveFeatureIndex(final int featureId, final Notification notification)
	{
		try
		{
			return indexFunction.index(featureId);
		}
		catch (RuntimeException e)
		{
			if (notification.type() == Notification.EventType.CONTAINER)
			{
				return -1;
			}
			throw e;
		}
	}

	@Override
	public <Callback extends IModelListener> void listen(final Callback listener,
														 final Feature<?, ?, ? super Callback, ? super Type> feature)
	{
		listenInternal(listener, IntStream.of(feature.id()).map(this::IDtoIndex));
	}

	@Override
	public <Callback extends IModelListener> void listen(final Callback listener,
														 final Collection<? extends Feature<?, ?, ? super Callback, ? super Type>> features)
	{
		listenInternal(listener, features.stream().mapToInt(this::featureToIndex));
	}

	@Override
	public <Callback extends IModelListener> void sulk(final Callback listener,
													   final Feature<?, ?, ? super Callback, ? super Type> feature)
	{
		sulkInternal(listener, IntStream.of(feature.id()).map(this::IDtoIndex));
	}

	@Override
	public <Callback extends IModelListener> void sulk(final Callback listener,
													   final Collection<? extends Feature<?, ?, ? super Callback, ? super Type>> features)
	{
		sulkInternal(listener, features.stream().mapToInt(this::featureToIndex));
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

	@Override
	public void listenStructure(Consumer<Notification> listener)
	{
		listenStructureInternal(listener);
	}

	@Override
	public void sulkStructure(Consumer<Notification> listener)
	{
		sulkStructureInternal(listener);
	}

	@Override
	public void listenStructureNoParam(Runnable listener)
	{
		listenStructureInternal(listener);
	}

	@Override
	public void sulkStructureNoParam(Runnable listener)
	{
		sulkStructureInternal(listener);
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

	private void listenStructureInternal(Object listener)
	{
		if (structureListeners == null)
		{
			structureListeners = new ConcurrentLinkedDeque<>();
		}
		structureListeners.add(listener);
	}

	private void sulkStructureInternal(Object listener)
	{
		if (structureListeners != null)
		{
			structureListeners.remove(listener);
		}
	}

	@FunctionalInterface
	public interface IndexFunction
	{
		int index(int id);
	}
}
