package org.logoce.lmf.model.notification.util;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.notification.impl.RelationAddManyNotifiation;
import org.logoce.lmf.model.notification.impl.RelationAddNotifiation;
import org.logoce.lmf.model.notification.impl.RelationRemoveManyNotifiation;
import org.logoce.lmf.model.notification.impl.RelationRemoveNotifiation;

import java.util.List;
import java.util.function.Consumer;

public final class ModelObserver
{
	private final List<RawFeature<?, ?>> features;
	private final HierarchyNotificationListener rootListener;
	private final Consumer<Notification> listener;

	private boolean deliver = true;

	public ModelObserver(Consumer<Notification> listener, RawFeature<?, ?> structuralFeatureId)
	{
		this(listener, List.of(structuralFeatureId));
	}

	public ModelObserver(Consumer<Notification> listener, List<RawFeature<?, ?>> structuralFeatures)
	{
		this.listener = listener;
		this.features = List.copyOf(structuralFeatures);
		rootListener = new HierarchyNotificationListener(0);
	}

	/**
	 * @param deliver Enable or disable the notifications.
	 */
	public void setDeliver(boolean deliver)
	{
		this.deliver = deliver;
	}

	public void startObserve(LMObject root)
	{
		rootListener.setTarget(root);
		root.listen(rootListener, features.get(0));
	}

	public void stopObserve(LMObject root)
	{
		root.sulk(rootListener, features.get(0));
		rootListener.unsetTarget(root);
	}

	private final class HierarchyNotificationListener implements Consumer<Notification>
	{
		private final int depth;
		private final RawFeature<?, ?> subFeature;
		private final HierarchyNotificationListener childListener;

		public HierarchyNotificationListener(int depth)
		{
			this.depth = depth;
			this.subFeature = computeSubFeature();
			if (depth != features.size() - 1)
			{
				childListener = new HierarchyNotificationListener(depth + 1);
			}
			else
			{
				childListener = null;
			}
		}

		private RawFeature<?, ?> computeSubFeature()
		{
			if (depth == features.size() - 1)
			{
				return null;
			}
			else
			{
				return features.get(depth + 1);
			}
		}

		private void setTarget(LMObject target)
		{
			final var feature = features.get(depth);
			final var value = getValue(target, feature);
			if (value == null) return;

			if (isFinalDepth())
			{
				if (deliver)
				{
					final var notif = feature.many()
									  ? new RelationAddManyNotifiation(target, feature, List.of(value))
									  : new RelationAddNotifiation(target, feature, value);
					ModelObserver.this.listener.accept(notif);
				}
			}
			else if (feature.relation())
			{
				try
				{
					actOnChildren(feature, value, this::addChild);
				}
				catch (IllegalArgumentException e)
				{
					throw new AssertionError("Error when exploring feature " +
											 feature.featureSupplier().get().name() +
											 " on " +
											 target.lmGroup().name(), e);
				}
			}
			else
			{
				throw new IllegalArgumentException("Observation failed, Feature " +
												   feature.featureSupplier().get().name() +
												   " on class " +
												   target.lmGroup().name() +
												   " is not a Relation.");
			}
		}

		private void unsetTarget(LMObject target)
		{
			final var feature = features.get(depth);
			final var value = getValue(target, feature);
			if (value == null) return;

			if (isFinalDepth())
			{
				if (deliver)
				{
					final var notif = feature.many()
									  ? new RelationRemoveManyNotifiation(target,
																		  feature,
																		  List.of(value))
									  : new RelationRemoveNotifiation(target, feature, value);
					ModelObserver.this.listener.accept(notif);
				}
			}
			else
			{
				actOnChildren(feature, value, this::removeChild);
			}
		}

		@SuppressWarnings("unchecked")
		private static void actOnChildren(RawFeature<?, ?> feature, Object value, Consumer<LMObject> action)
		{
			if (feature.many() == false)
			{
				action.accept((LMObject) value);
			}
			else
			{
				final var list = (List<LMObject>) value;
				for (int i = 0; i < list.size(); i++)
				{
					action.accept(list.get(i));
				}
			}
		}

		private static LMObject getValue(LMObject target, final RawFeature<?, ?> feature)
		{
			return (LMObject) target.get(feature.featureSupplier().get());
		}

		@Override
		public void accept(Notification notification)
		{
			if (isFinalDepth())
			{
				if (deliver)
				{
					ModelObserver.this.listener.accept(notification);
				}
			}
			else
			{
				NotificationUnifier.unify(notification, this::addChild, this::removeChild);
			}
		}

		private boolean isFinalDepth()
		{
			return depth == features.size() - 1;
		}

		private void addChild(final LMObject child)
		{
			child.listen(childListener, subFeature);
			childListener.setTarget(child);
		}

		private void removeChild(final LMObject child)
		{
			childListener.unsetTarget(child);
			child.sulk(childListener, subFeature);
		}
	}
}
