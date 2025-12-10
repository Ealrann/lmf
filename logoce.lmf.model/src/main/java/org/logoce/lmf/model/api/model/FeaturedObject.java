package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.notification.impl.ContainerChange;
import org.logoce.lmf.model.notification.impl.RelationNotificationBuilder;
import org.logoce.lmf.model.notification.impl.SetNotifiation;
import org.logoce.lmf.model.notification.list.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class FeaturedObject extends AdaptableStructureObject implements IFeaturedObject
{
	private final List<Consumer<Notification>> structureListeners = new ArrayList<>();
	private LMObject container;
	private int containingFeatureId = -1;

	public FeaturedObject()
	{}

	@Override
	public final LMObject lmContainer()
	{
		return container;
	}

	@Override
	public final Relation<?, ?> lmContainingFeature()
	{
		if (container == null || containingFeatureId == -1) return null;

		final var group = container.lmGroup();
		for (final var feature : group.features())
		{
			if (feature.id() == containingFeatureId)
			{
				return (Relation<?, ?>) feature;
			}
		}

		throw new IllegalStateException("Cannot resolve containing feature id " + containingFeatureId +
										" for group " + group.name());
	}

	private void structureNotify(final Notification notification)
	{
		for (final var listener : structureListeners)
		{
			listener.accept(notification);
		}
	}

	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return internalGet(feature);
	}

	@Override
	public <T> void set(final Feature<?, T> feature, final T value)
	{
		internalSet(feature, value);
	}

	@SuppressWarnings("unchecked")
	private <T, O> void internalSet(final Feature<?, T> feature, final T value)
	{
		final var getMap = (FeatureGetter<O>) getterMap();
		final var setMap = (FeatureSetter<O>) setterMap();

		final int featureId = feature.id();
		final var oldValue = getMap.get((O) this, featureId);
		setMap.set((O) this, featureId, value);
		final var notification = new SetNotifiation((LMObject) this, feature.id(), value, oldValue);
		if (feature instanceof Relation<?, ?> relation && relation.contains())
		{
			structureNotify(notification);
		}
		super.eNotify(notification);
	}

	@SuppressWarnings("unchecked")
	private <T, O> T internalGet(final Feature<?, T> feature)
	{
		final var getMap = (FeatureGetter<O>) getterMap();
		return getMap.get((O) this, feature.id());
	}

	protected FeatureGetter<?> getterMap() {return null;}

	protected FeatureSetter<?> setterMap() {return null;}

	/**
	 * Map a feature id to its stable index for this object.
	 * Implemented by generated implementations and by DynamicModelPackage.
	 */
	@Override
	public int featureIndex(final int featureId)
	{
		throw new UnsupportedOperationException("featureIndex not implemented");
	}

	protected final <E> List<E> newObservableList(final int featureId,
												  final boolean isRelation,
												  final boolean isContainment)
	{
		return new ObservableList<>(new ObservableListHandler<>(this, featureId, isRelation, isContainment));
	}

	protected final void setContainer(final LMObject child, final int featureId)
	{
		if (child != null)
		{
			ContainmentUtils.setContainer((LMObject) this, child, featureId);
		}
	}

	protected final void setContainer(final List<? extends LMObject> children, int featureId)
	{
		if (!children.isEmpty())
		{
			ContainmentUtils.setContainer((LMObject) this, children, featureId);
		}
	}

	@Override
	public final void listenStruture(final Consumer<Notification> listener)
	{
		structureListeners.add(listener);
	}

	@Override
	public final void sulkStructure(final Consumer<Notification> listener)
	{
		structureListeners.remove(listener);
	}

	private record ObservableListHandler<E>(FeaturedObject owner,
										   int featureId,
										   boolean relation,
										   boolean containment)
			implements BiConsumer<Notification.EventType, List<E>>
	{
		@Override
		public void accept(final Notification.EventType eventType, final List<E> elements)
		{
			if (elements.isEmpty()) return;

			final Notification notification;

			if (!relation)
			{
				Object newValue = null;
				Object oldValue = null;

				switch (eventType)
				{
					case ADD, ADD_MANY ->
							newValue = elements.size() == 1 ? elements.getFirst() : List.copyOf(elements);
					case REMOVE, REMOVE_MANY ->
							oldValue = elements.size() == 1 ? elements.getFirst() : List.copyOf(elements);
					default -> {
						return;
					}
				}

				notification = new SetNotifiation((LMObject) owner, featureId, newValue, oldValue);
			}
			else
			{
				@SuppressWarnings("unchecked")
				final var children = (List<? extends LMObject>) elements;

				if (containment && (eventType == Notification.EventType.ADD ||
									eventType == Notification.EventType.ADD_MANY))
				{
					if (eventType == Notification.EventType.ADD)
					{
						owner.setContainer(children.getFirst(), featureId);
					}
					else
					{
						owner.setContainer(children, featureId);
					}
				}

				notification = switch (eventType)
				{
					case ADD -> RelationNotificationBuilder.insert((LMObject) owner, featureId, children.getFirst());
					case ADD_MANY -> RelationNotificationBuilder.insert((LMObject) owner, featureId, children);
					case REMOVE -> RelationNotificationBuilder.remove((LMObject) owner, featureId, children.getFirst());
					case REMOVE_MANY -> RelationNotificationBuilder.remove((LMObject) owner, featureId, children);
					default -> null;
				};

				if (notification == null) return;

				if (containment)
				{
					owner.structureNotify(notification);
				}
			}

			owner.eNotify(notification);
		}
	}

	protected static final class ContainmentUtils
	{
		private ContainmentUtils()
		{
		}

		public static void setContainer(final LMObject newContainer, final LMObject child,
										final int featureId)
		{
			setContainerInternal(newContainer, child, featureId);
		}

		public static void setContainer(final LMObject newContainer,
										final List<? extends LMObject> children,
										final int featureId)
		{
			for (final var child : children)
			{
				setContainerInternal(newContainer, child, featureId);
			}
		}

		private static void setContainerInternal(final LMObject newContainer,
												 final LMObject child,
												 final int newFeatureId)
		{
			final var featuredChild = (FeaturedObject) child;
			final var oldContainer = featuredChild.container;
			final int oldFeatureId = featuredChild.containingFeatureId;

			featuredChild.container = newContainer;
			featuredChild.containingFeatureId = newFeatureId;

			if (oldContainer != null)
			{
				if (oldFeatureId != -1)
				{
					final var oldGroup = oldContainer.lmGroup();
					if (oldGroup != null)
					{
						final var oldParentNotification = RelationNotificationBuilder.remove(oldContainer,
																							 oldFeatureId,
																							 child);
						((FeaturedObject) oldContainer).structureNotify(oldParentNotification);
					}
				}
			}

			final var childNotification = new ContainerChange(child,
															  oldFeatureId,
															  newFeatureId,
															  newContainer,
															  oldContainer);
			featuredChild.structureNotify(childNotification);
		}
	}
}
