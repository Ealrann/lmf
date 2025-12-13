package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.notification.impl.BooleanSetNotification;
import org.logoce.lmf.model.notification.impl.DoubleSetNotification;
import org.logoce.lmf.model.notification.impl.FloatSetNotification;
import org.logoce.lmf.model.notification.impl.IntSetNotification;
import org.logoce.lmf.model.notification.impl.LongSetNotification;
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
import java.util.function.Supplier;

public final class ModelNotifier<Type extends IFeatures<?>> implements IModelNotifier.Impl<Type>
{
	private final LMObject notifier;
	private final int featureCount;
	private final IndexFunction indexFunction;

	private Deque<Object>[] notificationListeners = null;
	private Deque<Object> structureListeners = null;

	private Deque<IModelListener>[] modelListeners = null;

	private boolean deliver;

	public ModelNotifier(final LMObject notifier, final int featureCount, final IndexFunction indexFunction)
	{
		this.notifier = notifier;
		this.featureCount = featureCount;
		this.indexFunction = indexFunction;
	}

	@Override
	public boolean eDeliver()
	{
		return deliver;
	}

	@Override
	public void eDeliver(final boolean deliver)
	{
		this.deliver = deliver;
	}

	@Override
	public void notify(final int featureId,
					   final boolean isContainment,
					   final boolean isMany,
					   final Object oldValue,
					   final Object newValue)
	{
		notify(featureId, isContainment, isMany, Notification.EventType.SET, oldValue, newValue);
	}

	@Override
	public void notify(final int featureId,
					   final boolean isContainment,
					   final boolean isMany,
					   final Notification.EventType eventType,
					   final Object oldValue,
					   final Object newValue)
	{
		if (!deliver) return;

		Notification notification = null;
		final Supplier<Notification> notificationSupplier = () -> new InlineNotification(notifier,
																						 isContainment,
																						 featureId,
																						 eventType,
																						 newValue,
																						 oldValue);

		if (isContainment && structureListeners != null)
		{
			notification = dispatchNotificationListeners(structureListeners, notification, notificationSupplier);
		}

		if (notificationListeners == null && modelListeners == null) return;

		final int featureIdx = resolveFeatureIndex(featureId);
		if (featureIdx < 0) return;

		final var featureNotificationListeners = notificationListeners == null ? null : notificationListeners[featureIdx];
		if (featureNotificationListeners != null)
		{
			notification = dispatchNotificationListeners(featureNotificationListeners, notification, notificationSupplier);
		}

		final var featureModelListeners = modelListeners == null ? null : modelListeners[featureIdx];
		if (featureModelListeners != null)
		{
			final var normalizedOldValue = normalize(isMany, oldValue);
			final var normalizedNewValue = normalize(isMany, newValue);
			for (final var listener : featureModelListeners)
			{
				@SuppressWarnings("unchecked") final var typedListener = (Listener<Object>) listener;
				typedListener.notify(normalizedOldValue, normalizedNewValue);
			}
		}
	}

	@Override
	public void notifyInt(final int featureId,
						  final boolean isContainment,
						  final boolean isMany,
						  final int oldValue,
						  final int newValue)
	{
		if (!deliver) return;

		Notification notification = null;
		final Supplier<Notification> notificationSupplier = () -> new IntSetNotification(notifier,
																						  isContainment,
																						  featureId,
																						  newValue,
																						  oldValue);

		if (isContainment && structureListeners != null)
		{
			notification = dispatchNotificationListeners(structureListeners, notification, notificationSupplier);
		}

		if (notificationListeners == null && modelListeners == null) return;

		final int featureIdx = resolveFeatureIndex(featureId);
		if (featureIdx < 0) return;

		final var featureNotificationListeners = notificationListeners == null ? null : notificationListeners[featureIdx];
		if (featureNotificationListeners != null)
		{
			notification = dispatchNotificationListeners(featureNotificationListeners, notification, notificationSupplier);
		}

		final var featureModelListeners = modelListeners == null ? null : modelListeners[featureIdx];
		if (featureModelListeners != null)
		{
			for (final var listener : featureModelListeners)
			{
				((IntListener) listener).notify(oldValue, newValue);
			}
		}
	}

	@Override
	public void notifyLong(final int featureId,
						   final boolean isContainment,
						   final boolean isMany,
						   final long oldValue,
						   final long newValue)
	{
		if (!deliver) return;

		Notification notification = null;
		final Supplier<Notification> notificationSupplier = () -> new LongSetNotification(notifier,
																						   isContainment,
																						   featureId,
																						   newValue,
																						   oldValue);

		if (isContainment && structureListeners != null)
		{
			notification = dispatchNotificationListeners(structureListeners, notification, notificationSupplier);
		}

		if (notificationListeners == null && modelListeners == null) return;

		final int featureIdx = resolveFeatureIndex(featureId);
		if (featureIdx < 0) return;

		final var featureNotificationListeners = notificationListeners == null ? null : notificationListeners[featureIdx];
		if (featureNotificationListeners != null)
		{
			notification = dispatchNotificationListeners(featureNotificationListeners, notification, notificationSupplier);
		}

		final var featureModelListeners = modelListeners == null ? null : modelListeners[featureIdx];
		if (featureModelListeners != null)
		{
			for (final var listener : featureModelListeners)
			{
				((LongListener) listener).notify(oldValue, newValue);
			}
		}
	}

	@Override
	public void notifyBoolean(final int featureId,
							  final boolean isContainment,
							  final boolean isMany,
							  final boolean oldValue,
							  final boolean newValue)
	{
		if (!deliver) return;

		Notification notification = null;
		final Supplier<Notification> notificationSupplier = () -> new BooleanSetNotification(notifier,
																							  isContainment,
																							  featureId,
																							  newValue,
																							  oldValue);

		if (isContainment && structureListeners != null)
		{
			notification = dispatchNotificationListeners(structureListeners, notification, notificationSupplier);
		}

		if (notificationListeners == null && modelListeners == null) return;

		final int featureIdx = resolveFeatureIndex(featureId);
		if (featureIdx < 0) return;

		final var featureNotificationListeners = notificationListeners == null ? null : notificationListeners[featureIdx];
		if (featureNotificationListeners != null)
		{
			notification = dispatchNotificationListeners(featureNotificationListeners, notification, notificationSupplier);
		}

		final var featureModelListeners = modelListeners == null ? null : modelListeners[featureIdx];
		if (featureModelListeners != null)
		{
			for (final var listener : featureModelListeners)
			{
				((BooleanListener) listener).notify(oldValue, newValue);
			}
		}
	}

	@Override
	public void notifyFloat(final int featureId,
							final boolean isContainment,
							final boolean isMany,
							final float oldValue,
							final float newValue)
	{
		if (!deliver) return;

		Notification notification = null;
		final Supplier<Notification> notificationSupplier = () -> new FloatSetNotification(notifier,
																							isContainment,
																							featureId,
																							newValue,
																							oldValue);

		if (isContainment && structureListeners != null)
		{
			notification = dispatchNotificationListeners(structureListeners, notification, notificationSupplier);
		}

		if (notificationListeners == null && modelListeners == null) return;

		final int featureIdx = resolveFeatureIndex(featureId);
		if (featureIdx < 0) return;

		final var featureNotificationListeners = notificationListeners == null ? null : notificationListeners[featureIdx];
		if (featureNotificationListeners != null)
		{
			notification = dispatchNotificationListeners(featureNotificationListeners, notification, notificationSupplier);
		}

		final var featureModelListeners = modelListeners == null ? null : modelListeners[featureIdx];
		if (featureModelListeners != null)
		{
			for (final var listener : featureModelListeners)
			{
				((FloatListener) listener).notify(oldValue, newValue);
			}
		}
	}

	@Override
	public void notifyDouble(final int featureId,
							 final boolean isContainment,
							 final boolean isMany,
							 final double oldValue,
							 final double newValue)
	{
		if (!deliver) return;

		Notification notification = null;
		final Supplier<Notification> notificationSupplier = () -> new DoubleSetNotification(notifier,
																							 isContainment,
																							 featureId,
																							 newValue,
																							 oldValue);

		if (isContainment && structureListeners != null)
		{
			notification = dispatchNotificationListeners(structureListeners, notification, notificationSupplier);
		}

		if (notificationListeners == null && modelListeners == null) return;

		final int featureIdx = resolveFeatureIndex(featureId);
		if (featureIdx < 0) return;

		final var featureNotificationListeners = notificationListeners == null ? null : notificationListeners[featureIdx];
		if (featureNotificationListeners != null)
		{
			notification = dispatchNotificationListeners(featureNotificationListeners, notification, notificationSupplier);
		}

		final var featureModelListeners = modelListeners == null ? null : modelListeners[featureIdx];
		if (featureModelListeners != null)
		{
			for (final var listener : featureModelListeners)
			{
				((DoubleListener) listener).notify(oldValue, newValue);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static Notification dispatchNotificationListeners(final Deque<Object> listeners,
															 final Notification notification,
															 final Supplier<Notification> notificationSupplier)
	{
		var current = notification;
		for (final var listener : listeners)
		{
			if (listener instanceof Runnable runnable)
			{
				runnable.run();
			}
			else
			{
				if (current == null)
				{
					current = notificationSupplier.get();
				}
				((Consumer<Notification>) listener).accept(current);
			}
		}
		return current;
	}

	private static Object normalize(final boolean isMany, final Object value)
	{
		if (!isMany)
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

	private int resolveFeatureIndex(final int featureId)
	{
		try
		{
			return indexFunction.index(featureId);
		}
		catch (RuntimeException e)
		{
			return -1;
		}
	}

	@Override
	public <Callback extends IModelListener> void listen(final Callback listener,
														 final Feature<?, ?, ? super Callback, ? super Type> feature)
	{
		listenModelListener(listener, feature.id());
	}

	@Override
	public <Callback extends IModelListener> void listen(final Callback listener,
														 final Collection<? extends Feature<?, ?, ? super Callback, ? super Type>> features)
	{
		for (final var feature : features)
		{
			listenModelListener(listener, feature.id());
		}
	}

	@Override
	public <Callback extends IModelListener> void sulk(final Callback listener,
													   final Feature<?, ?, ? super Callback, ? super Type> feature)
	{
		sulkModelListener(listener, feature.id());
	}

	@Override
	public <Callback extends IModelListener> void sulk(final Callback listener,
													   final Collection<? extends Feature<?, ?, ? super Callback, ? super Type>> features)
	{
		for (final var feature : features)
		{
			sulkModelListener(listener, feature.id());
		}
	}

	private void listenModelListener(final IModelListener listener, final int featureId)
	{
		final int featureIdx = resolveFeatureIndex(featureId);
		if (featureIdx < 0) return;

		modelListeners = registerListener(modelListeners, featureIdx, listener);
	}

	private void sulkModelListener(final IModelListener listener, final int featureId)
	{
		final int featureIdx = resolveFeatureIndex(featureId);
		if (featureIdx < 0) return;

		unregisterListener(modelListeners, featureIdx, listener);
	}

	@Override
	public void listen(final Consumer<Notification> listener, final int... featureIDs)
	{
		for (final int featureId : featureIDs)
		{
			listenNotificationListener(listener, featureId);
		}
	}

	@Override
	public void sulk(final Consumer<Notification> listener, final int... featureIDs)
	{
		for (final int featureId : featureIDs)
		{
			sulkNotificationListener(listener, featureId);
		}
	}

	@Override
	public void listenNoParam(final Runnable listener, final int... featureIDs)
	{
		for (final int featureId : featureIDs)
		{
			listenNotificationListener(listener, featureId);
		}
	}

	@Override
	public void sulkNoParam(final Runnable listener, final int... featureIDs)
	{
		for (final int featureId : featureIDs)
		{
			sulkNotificationListener(listener, featureId);
		}
	}

	@Override
	public void listen(final Consumer<Notification> listener, final List<Feature<?, ?, ?, ?>> features)
	{
		for (final var feature : features)
		{
			listenNotificationListener(listener, feature.id());
		}
	}

	@Override
	public void sulk(final Consumer<Notification> listener, final List<Feature<?, ?, ?, ?>> features)
	{
		for (final var feature : features)
		{
			sulkNotificationListener(listener, feature.id());
		}
	}

	@Override
	public void listenNoParam(final Runnable listener, final List<Feature<?, ?, ?, ?>> features)
	{
		for (final var feature : features)
		{
			listenNotificationListener(listener, feature.id());
		}
	}

	@Override
	public void sulkNoParam(final Runnable listener, final List<Feature<?, ?, ?, ?>> features)
	{
		for (final var feature : features)
		{
			sulkNotificationListener(listener, feature.id());
		}
	}

	@Override
	public void listenStructure(final Consumer<Notification> listener)
	{
		listenStructureInternal(listener);
	}

	@Override
	public void sulkStructure(final Consumer<Notification> listener)
	{
		sulkStructureInternal(listener);
	}

	@Override
	public void listenStructureNoParam(final Runnable listener)
	{
		listenStructureInternal(listener);
	}

	@Override
	public void sulkStructureNoParam(final Runnable listener)
	{
		sulkStructureInternal(listener);
	}

	private void listenStructureInternal(final Object listener)
	{
		if (structureListeners == null)
		{
			structureListeners = new ConcurrentLinkedDeque<>();
		}
		structureListeners.add(listener);
	}

	private void sulkStructureInternal(final Object listener)
	{
		if (structureListeners == null) return;
		structureListeners.remove(listener);
	}

	private void listenNotificationListener(final Object listener, final int featureId)
	{
		final int featureIdx = resolveFeatureIndex(featureId);
		if (featureIdx < 0) return;

		notificationListeners = registerListener(notificationListeners, featureIdx, listener);
	}

	private void sulkNotificationListener(final Object listener, final int featureId)
	{
		if (notificationListeners == null) return;

		final int featureIdx = resolveFeatureIndex(featureId);
		if (featureIdx < 0) return;

		unregisterListener(notificationListeners, featureIdx, listener);
	}

	private <ListenerType> Deque<ListenerType>[] registerListener(final Deque<ListenerType>[] map,
																  final int featureIdx,
																  final ListenerType listener)
	{
		final Deque<ListenerType>[] actualMap;
		if (map == null)
		{
			actualMap = initMap();
		}
		else
		{
			actualMap = map;
		}

		var list = actualMap[featureIdx];
		if (list == null)
		{
			list = new ConcurrentLinkedDeque<>();
			actualMap[featureIdx] = list;
		}
		list.add(listener);
		return actualMap;
	}

	private static <ListenerType> void unregisterListener(final Deque<ListenerType>[] map,
														  final int featureIdx,
														  final ListenerType listener)
	{
		if (map == null) return;

		final var list = map[featureIdx];
		if (list != null)
		{
			list.remove(listener);
		}
	}

	@SuppressWarnings("unchecked")
	private <ListenerType> Deque<ListenerType>[] initMap()
	{
		return new Deque[featureCount];
	}

	private record InlineNotification(LMObject notifier,
									  boolean isContainment,
									  int featureId,
									  EventType type,
									  Object newValue,
									  Object oldValue) implements Notification
	{
	}

	@FunctionalInterface
	public interface IndexFunction
	{
		int index(int id);
	}
}
