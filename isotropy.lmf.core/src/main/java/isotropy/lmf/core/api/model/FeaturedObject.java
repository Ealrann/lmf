package isotropy.lmf.core.api.model;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.api.notification.Notification;
import isotropy.lmf.core.notification.ContainerChange;
import isotropy.lmf.core.notification.RelationNotificationBuilder;
import isotropy.lmf.core.notification.SetNotifiation;
import isotropy.lmf.core.feature.FeatureGetter;
import isotropy.lmf.core.feature.FeatureSetter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class FeaturedObject implements IFeaturedObject
{
	private final List<Consumer<Notification>> structureListeners = new ArrayList<>();
	private LMObject container;
	private RawFeature<?, ?> containingFeature;

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
		return (Relation<?, ?>) containingFeature.featureSupplier().get();
	}

	private void lNotify(final Notification notification)
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
		lNotify(new SetNotifiation((LMObject) this, feature.rawFeature(), value, oldValue));
	}

	@SuppressWarnings("unchecked")
	private <T, O> T internalGet(final Feature<?, T> feature)
	{
		final var getMap = (FeatureGetter<O>) getterMap();
		return getMap.get((O) this, feature.rawFeature());
	}

	protected FeatureGetter<?> getterMap() {return null;}

	protected FeatureSetter<?> setterMap() {return null;}

	protected final void setContainer(final LMObject child, final RawFeature<?, ?> feature)
	{
		if (child != null)
		{
			ContainmentUtils.setContainer((LMObject) this, child, feature);
		}
	}

	protected final void setContainer(final List<? extends LMObject> children, final RawFeature<?, ?> feature)
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

	protected static final class ContainmentUtils
	{
		public static void setContainer(LMObject newContainer, LMObject child, RawFeature<?, ?> feature)
		{
			setContainerInternal(newContainer, child, feature);
		}

		public static void setContainer(LMObject newContainer,
										List<? extends LMObject> children,
										RawFeature<?, ?> newFeature)
		{
			for (final var child : children)
			{
				setContainerInternal(newContainer, child, newFeature);
			}
		}

		private static void setContainerInternal(final LMObject newContainer,
												 final LMObject child,
												 final RawFeature<?, ?> newFeature)
		{
			final var featuredChild = (FeaturedObject) child;
			final var oldContainer = featuredChild.container;
			final var oldFeature = featuredChild.containingFeature;

			featuredChild.containingFeature = newFeature;
			featuredChild.container = newContainer;

			if (oldContainer != null)
			{
				final var oldParentNotification = RelationNotificationBuilder.remove(oldContainer, oldFeature, child);
				((FeaturedObject) oldContainer).lNotify(oldParentNotification);
			}

			final var childNotification = new ContainerChange(child,
															  oldFeature,
															  newFeature,
															  newContainer,
															  oldContainer);
			featuredChild.lNotify(childNotification);
		}
	}
}
