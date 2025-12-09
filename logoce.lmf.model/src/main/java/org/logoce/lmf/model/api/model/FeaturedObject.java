package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.api.feature.RawFeature;
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
	private Feature<?, ?> containingFeature;

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
		return (Relation<?, ?>) containingFeature;
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

		final var oldValue = getMap.get((O) this, feature.rawFeature());
		setMap.set((O) this, feature.rawFeature(), value);
		final var notification = new SetNotifiation((LMObject) this, feature, value, oldValue);
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
		return getMap.get((O) this, feature.rawFeature());
	}

	protected FeatureGetter<?> getterMap() {return null;}

	protected FeatureSetter<?> setterMap() {return null;}

	protected final <E> List<E> newObservableList(final Feature<E, ?> feature)
	{
		final var isRelation = feature instanceof Relation;
		final var isContainment = feature instanceof Relation<?, ?> rel && rel.contains();
		return new ObservableList<>(new ObservableListHandler<>(this, feature, isRelation, isContainment));
	}

	// Legacy overloads for generated implementations still passing RawFeature
	protected final void setContainer(final LMObject child, final RawFeature<?, ?> feature)
	{
		if (child != null)
		{
			ContainmentUtils.setContainer((LMObject) this, child, feature.featureSupplier().get());
		}
	}

	protected final void setContainer(final List<? extends LMObject> children, final RawFeature<?, ?> feature)
	{
		if (!children.isEmpty())
		{
			ContainmentUtils.setContainer((LMObject) this, children, feature.featureSupplier().get());
		}
	}

	protected final void setContainer(final LMObject child, final Feature<?, ?> feature)
	{
		if (child != null)
		{
			ContainmentUtils.setContainer((LMObject) this, child, feature);
		}
	}

	protected final void setContainer(final List<? extends LMObject> children, final Feature<?, ?> feature)
	{
		if (!children.isEmpty())
		{
			ContainmentUtils.setContainer((LMObject) this, children, feature);
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
										   Feature<E, ?> feature,
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

				notification = new SetNotifiation((LMObject) owner, feature, newValue, oldValue);
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
						owner.setContainer(children.getFirst(), feature);
					}
					else
					{
						owner.setContainer(children, feature);
					}
				}

				notification = switch (eventType)
				{
					case ADD -> RelationNotificationBuilder.insert((LMObject) owner, feature, children.getFirst());
					case ADD_MANY -> RelationNotificationBuilder.insert((LMObject) owner, feature, children);
					case REMOVE -> RelationNotificationBuilder.remove((LMObject) owner, feature, children.getFirst());
					case REMOVE_MANY -> RelationNotificationBuilder.remove((LMObject) owner, feature, children);
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
										final Feature<?, ?> feature)
		{
			setContainerInternal(newContainer, child, feature);
		}

		public static void setContainer(final LMObject newContainer,
										final List<? extends LMObject> children,
										final Feature<?, ?> newFeature)
		{
			for (final var child : children)
			{
				setContainerInternal(newContainer, child, newFeature);
			}
		}

		private static void setContainerInternal(final LMObject newContainer,
												 final LMObject child,
												 final Feature<?, ?> newFeature)
		{
			final var featuredChild = (FeaturedObject) child;
			final var oldContainer = featuredChild.container;
			final var oldFeature = featuredChild.containingFeature;

			featuredChild.containingFeature = newFeature;
			featuredChild.container = newContainer;

			if (oldContainer != null)
			{
				final var oldParentNotification = RelationNotificationBuilder.remove(oldContainer,
																					 oldFeature,
																					 child);
				((FeaturedObject) oldContainer).structureNotify(oldParentNotification);
			}

			final var childNotification = new ContainerChange(child,
															  oldFeature,
															  newFeature,
															  newContainer,
															  oldContainer);
			featuredChild.structureNotify(childNotification);
		}
	}
}
