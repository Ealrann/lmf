package org.logoce.lmf.model.api.model;

import org.logoce.lmf.adapter.api.BasicAdapterManager;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Named;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.notification.impl.ContainerChange;
import org.logoce.lmf.model.notification.impl.RelationNotificationBuilder;
import org.logoce.lmf.model.notification.impl.SetNotification;
import org.logoce.lmf.model.notification.list.ObservableList;
import org.logoce.lmf.model.notification.util.NotificationUnifier;
import org.logoce.lmf.model.util.ModelUtil;
import org.logoce.lmf.model.util.oldlogoce.TreeLazyIterator;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class FeaturedObject<F extends IFeaturedObject.Features<?>> implements IFeaturedObject
{
	private LMObject container;
	private int containingFeatureId = -1;
	private BasicAdapterManager extenderManager = null;
	private boolean loaded = false;

	public FeaturedObject()
	{}

	@Override
	public abstract IModelNotifier.Impl<? extends IFeaturedObject.Features<?>> notifier();

	@Override
	public final LMObject lmContainer()
	{
		return container;
	}

	@Override
	public final Relation<?, ?, ?, ?> lmContainingFeature()
	{
		if (container == null || containingFeatureId == -1) return null;
		final var group = container.lmGroup();
		final int featureIndex = container.featureIndex(containingFeatureId);
		return (Relation<?, ?, ?, ?>) group.features().get(featureIndex);
	}

	@Override
	public final int lmContainingFeatureID()
	{
		return containingFeatureId;
	}

	@Override
	public <T> T get(final Feature<?, ?, ?, ?> feature)
	{
		return internalGet(feature.id());
	}

	@Override
	public Object get(final int featureID)
	{
		return internalGet(featureID);
	}

	@SuppressWarnings("unchecked")
	private <T, O> T internalGet(final int featureID)
	{
		final var getMap = (FeatureGetter<O>) getterMap();
		return getMap.get((O) this, featureID);
	}

	@Override
	public <T> void set(final Feature<?, ?, ?, ?> feature, final T value)
	{
		@SuppressWarnings("unchecked") final var typedFeature = (Feature<?, T, ?, ?>) feature;
		internalSet(typedFeature, value);
	}

	@Override
	public void set(final int featureID, final Object value)
	{
		final var feature = lmGroup().features().get(featureIndex(featureID));
		castSet(feature, value);
	}

	@SuppressWarnings("unchecked")
	private <T> void castSet(final Feature<?, ?, ?, ?> feature, final Object value)
	{
		internalSet((Feature<?, T, ?, ?>) feature, (T) value);
	}

	@SuppressWarnings("unchecked")
	private <T, O> void internalSet(final Feature<?, T, ?, ?> feature, final T value)
	{
		final var getMap = (FeatureGetter<O>) getterMap();
		final var setMap = (FeatureSetter<O>) setterMap();

		final int featureId = feature.id();
		final boolean contains = feature instanceof Relation<?, ?, ?, ?> rel && rel.contains();
		final var oldValue = getMap.get((O) this, featureId);
		setMap.set((O) this, featureId, value);
		final var notification = new SetNotification((LMObject) this, contains, feature.id(), value, oldValue);
		eNotify(notification);
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
		notifier().listenStructure(listener);
	}

	@Override
	public final void sulkStructure(final Consumer<Notification> listener)
	{
		notifier().sulkStructure(listener);
	}

	private static final String CANNOT_FIND_ADAPTER = "Cannot find adapter [%s] for class [%s]";
	private static final String CANNOT_FIND_IDENTIFIED_ADAPTER = "Cannot find adapter [%s] (id = %s) for class [%s]";


	protected final void eNotify(final Notification notification)
	{
		final boolean isContainment = notification.isContainment();
		if (isContainment) NotificationUnifier.unifyAdded(notification, this::setupChild);
		notifier().notify(notification);
		if (isContainment) NotificationUnifier.unifyRemoved(notification, this::disposeChild);
	}

	private void setupChild(IFeaturedObject notifier)
	{
		if (loaded && notifier instanceof FeaturedObject<?> child)
		{
			child.loadExtenderManager();
		}
	}

	private void disposeChild(IFeaturedObject notifier)
	{
		if (loaded && notifier instanceof FeaturedObject<?> child)
		{
			child.disposeExtenderManager();
		}
	}

	@Override
	public final <T extends IAdapter> T adaptGeneric(final Class<? extends IAdapter> type)
	{
		@SuppressWarnings("unchecked") final var adapt = (T) adapt(type);
		return adapt;
	}

	@Override
	public final <T extends IAdapter> T adapt(final Class<T> type)
	{
		return adapterManager().adapt(type);
	}

	@Override
	public final <T extends IAdapter> T adapt(final Class<T> type, final String identifier)
	{
		return adapterManager().adapt(type, identifier);
	}

	@Override
	public final <T extends IAdapter> T adaptNotNullGeneric(final Class<? extends IAdapter> type)
	{
		@SuppressWarnings("unchecked") final var adapt = (T) adaptNotNull(type);
		return adapt;
	}

	@Override
	public final <T extends IAdapter> T adaptNotNull(final Class<T> type)
	{
		final T adapt = adapt(type);
		if (adapt == null)
		{
			var message = String.format(CANNOT_FIND_ADAPTER, type.getSimpleName(), lmGroup().name());
			if (this instanceof Named)
			{
				message += ": " + ((Named) this).name();
			}
			throw new NullPointerException(message);
		}
		return adapt;
	}

	@Override
	public final <T extends IAdapter> T adaptNotNull(final Class<T> type, final String identifier)
	{
		final T adapt = adapt(type, identifier);
		if (adapt == null)
		{
			var message = String.format(CANNOT_FIND_IDENTIFIED_ADAPTER,
										type.getSimpleName(),
										identifier,
										lmGroup().name());
			if (this instanceof Named)
			{
				message += ": " + ((Named) this).name();
			}
			throw new NullPointerException(message);
		}
		return adapt;
	}

	@Override
	public final BasicAdapterManager adapterManager()
	{
		if (extenderManager == null)
		{
			extenderManager = new BasicAdapterManager(this);
		}
		return extenderManager;
	}

	public final void loadExtenderManager()
	{
		treeIterator().forEachRemaining(object -> ((FeaturedObject<?>) object).load());
	}

	public final void disposeExtenderManager()
	{
		treeIterator().forEachRemaining(object -> ((FeaturedObject<?>) object).dispose());
	}

	private void load()
	{
		if (!loaded)
		{
			loaded = true;
			adapterManager().load();
		}
	}

	private void dispose()
	{
		if (loaded)
		{
			adapterManager().dispose();
			loaded = false;
		}
	}

	@Override
	public final Stream<LMObject> streamTree()
	{
		return StreamSupport.stream(treeIterator(), false);
	}

	private TreeLazyIterator treeIterator()
	{
		return new TreeLazyIterator((LMObject) this);
	}

	@Override
	public final Stream<LMObject> streamChildren()
	{
		return ModelUtil.streamContainmentFeatures(lmGroup()).map(Relation.class::cast).flatMap(this::streamReference);
	}

	@SuppressWarnings("unchecked")
	private Stream<LMObject> streamReference(Relation<?, ?, ?, ?> ref)
	{
		if (ref.many())
		{
			return ((List<LMObject>) ((LMObject) this).get(ref)).stream();
		}
		else
		{
			return Stream.ofNullable((LMObject) ((LMObject) this).get(ref));
		}
	}

	private record ObservableListHandler<E>(FeaturedObject<?> owner,
											int featureId,
											boolean relation,
											boolean containment) implements BiConsumer<Notification.EventType, List<E>>
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
					case ADD, ADD_MANY -> newValue = elements.size() == 1 ? elements.getFirst() : List.copyOf(elements);
					case REMOVE, REMOVE_MANY ->
							oldValue = elements.size() == 1 ? elements.getFirst() : List.copyOf(elements);
					default ->
					{
						return;
					}
				}

				notification = new SetNotification((LMObject) owner, containment, featureId, newValue, oldValue);
			}
			else
			{
				@SuppressWarnings("unchecked") final var children = (List<? extends LMObject>) elements;

				if (containment &&
					(eventType == Notification.EventType.ADD || eventType == Notification.EventType.ADD_MANY))
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
					case ADD -> RelationNotificationBuilder.insert((LMObject) owner,
																   featureId,
																   containment,
																   true,
																   children.getFirst());
					case ADD_MANY ->
							RelationNotificationBuilder.insert((LMObject) owner, featureId, containment, children);
					case REMOVE -> RelationNotificationBuilder.remove((LMObject) owner,
																	  featureId,
																	  containment,
																	  true,
																	  children.getFirst());
					case REMOVE_MANY ->
							RelationNotificationBuilder.remove((LMObject) owner, featureId, containment, children);
					default -> null;
				};

				if (notification == null) return;
			}

			owner.eNotify(notification);
		}
	}

	protected static final class ContainmentUtils
	{
		private ContainmentUtils()
		{
		}

		public static void setContainer(final LMObject newContainer, final LMObject child, final int featureId)
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
			final var featuredChild = (FeaturedObject<?>) child;
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
																							 true,
																							 true,
																							 child);
						((FeaturedObject<?>) oldContainer).eNotify(oldParentNotification);
					}
				}
			}

			final var childNotification = new ContainerChange(child,
															  oldFeatureId,
															  newFeatureId,
															  newContainer,
															  oldContainer);
			featuredChild.eNotify(childNotification);
		}
	}
}
