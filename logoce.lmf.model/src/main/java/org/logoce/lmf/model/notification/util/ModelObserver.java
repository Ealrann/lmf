package org.logoce.lmf.model.notification.util;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.notification.impl.RelationAddManyNotifiation;
import org.logoce.lmf.model.notification.impl.RelationAddNotifiation;
import org.logoce.lmf.model.notification.impl.RelationRemoveManyNotifiation;
import org.logoce.lmf.model.notification.impl.RelationRemoveNotifiation;

import java.util.List;
import java.util.function.Consumer;

public final class ModelObserver
{
	private final int[] features;
	private final HierarchyNotificationListener rootListener;
	private final Consumer<Notification> listener;

	private boolean deliver = true;

	public ModelObserver(final Consumer<Notification> listener, final Relation<?, ?> structuralFeature)
	{
		this(listener, new int[]{structuralFeature.id()});
	}

	public ModelObserver(final Consumer<Notification> listener, final List<Relation<?, ?>> structuralFeatures)
	{
		this(listener, structuralFeatures.stream().mapToInt(Relation::id).toArray());
	}

	public ModelObserver(final Consumer<Notification> listener, final int[] structuralFeatureIds)
	{
		this.listener = listener;
		this.features = structuralFeatureIds.clone();
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
		root.listen(rootListener, features[0]);
	}

	public void stopObserve(LMObject root)
	{
		root.sulk(rootListener, features[0]);
		rootListener.unsetTarget(root);
	}

	private final class HierarchyNotificationListener implements Consumer<Notification>
	{
		private final int depth;
		private final int subFeatureId;
		private final HierarchyNotificationListener childListener;

		public HierarchyNotificationListener(int depth)
		{
			this.depth = depth;
			this.subFeatureId = computeSubFeatureId();
			if (depth != features.length - 1)
			{
				childListener = new HierarchyNotificationListener(depth + 1);
			}
			else
			{
				childListener = null;
			}
		}

		private int computeSubFeatureId()
		{
			if (depth == features.length - 1)
			{
				return -1;
			}
			else
			{
				return features[depth + 1];
			}
		}

		private void setTarget(LMObject target)
		{
			final int featureId = features[depth];
			final var feature = resolveRelation(target, featureId);
			final var value = getValue(target, feature);
			if (value == null) return;

			if (isFinalDepth())
			{
				if (deliver)
				{
					final var notif = feature.many()
									  ? new RelationAddManyNotifiation(target, feature.id(), List.of(value))
									  : new RelationAddNotifiation(target, feature.id(), value);
					ModelObserver.this.listener.accept(notif);
				}
			}
			else
			{
				actOnChildren(feature, value, this::addChild);
			}
		}

		private void unsetTarget(LMObject target)
		{
			final int featureId = features[depth];
			final var feature = resolveRelation(target, featureId);
			final var value = getValue(target, feature);
			if (value == null) return;

			if (isFinalDepth())
			{
				if (deliver)
				{
					final var notif = feature.many()
									  ? new RelationRemoveManyNotifiation(target,
																		  feature.id(),
																		  List.of(value))
									  : new RelationRemoveNotifiation(target, feature.id(), value);
					ModelObserver.this.listener.accept(notif);
				}
			}
			else
			{
				actOnChildren(feature, value, this::removeChild);
			}
		}

		@SuppressWarnings("unchecked")
		private static void actOnChildren(final Relation<?, ?> feature, final Object value,
										  final Consumer<LMObject> action)
		{
			if (!feature.many())
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

		private static LMObject getValue(final LMObject target, final Relation<?, ?> feature)
		{
			return (LMObject) target.get(feature);
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
			return depth == features.length - 1;
		}

		private void addChild(final LMObject child)
		{
			child.listen(childListener, subFeatureId);
			childListener.setTarget(child);
		}

		private void removeChild(final LMObject child)
		{
			childListener.unsetTarget(child);
			child.sulk(childListener, subFeatureId);
		}

		private Relation<?, ?> resolveRelation(final LMObject target, final int featureId)
		{
			final var group = target.lmGroup();
			for (final var feature : group.features())
			{
				if (feature.id() == featureId)
				{
					if (feature instanceof Relation<?, ?> relation)
					{
						return relation;
					}
					throw new IllegalArgumentException(
							"Observation failed, feature " + featureId + " on group " + group.name() + " is not a Relation.");
				}
			}
			throw new IllegalArgumentException(
					"Unknown featureId " + featureId + " for group " + group.name());
		}
	}
}
