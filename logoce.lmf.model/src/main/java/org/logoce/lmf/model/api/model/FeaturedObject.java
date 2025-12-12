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
import org.logoce.lmf.model.notification.list.ObservableList;
import org.logoce.lmf.model.notification.util.NotificationUnifier;
import org.logoce.lmf.model.util.ModelUtil;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class FeaturedObject<F extends IFeaturedObject.Features<?>> implements IFeaturedObject
{
	LMObject container;
	int containingFeatureId = -1;
	private BasicAdapterManager extenderManager = null;
	boolean loaded = false;

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
		final var setMap = (FeatureSetter<O>) setterMap();
		setMap.set((O) this, feature.id(), value);
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
		return new ObservableList<>(ObservableListSupport.handler(this, featureId, isRelation, isContainment));
	}

	protected final void setContainer(final LMObject child, final int featureId)
	{
		if (child != null)
		{
			ContainmentSupport.setContainer((LMObject) this, child, featureId);
		}
	}

	protected final void setContainer(final List<? extends LMObject> children, int featureId)
	{
		if (!children.isEmpty())
		{
			ContainmentSupport.setContainer((LMObject) this, children, featureId);
		}
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
		AdapterLifecycleSupport.loadExtenderManager(this);
	}

	public final void disposeExtenderManager()
	{
		AdapterLifecycleSupport.disposeExtenderManager(this);
	}

	@Override
	public final Stream<LMObject> streamTree()
	{
		return StreamSupport.stream(AdapterLifecycleSupport.treeIterator(this), false);
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
			return ((List<LMObject>) this.get(ref)).stream();
		}
		else
		{
			return Stream.ofNullable(this.get(ref));
		}
	}
}
