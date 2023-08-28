package isotropy.lmf.core.model;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.model.notification.ContainerChange;
import isotropy.lmf.core.model.notification.RelationNotificationBuilder;

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
		return (Relation<?, ?>) containingFeature.featureSupplier()
												 .get();
	}

	private void lNotify(final Notification notification)
	{
		for (final var listener : structureListeners)
		{
			listener.accept(notification);
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
		public static void setContainer(LMObject parent, List<? extends LMObject> children, RawFeature<?, ?> feature)
		{
			changeContainer(null, parent, children, null, feature);
		}

		public static void setContainer(LMObject parent, LMObject child, RawFeature<?, ?> feature)
		{
			changeContainer(null, parent, child, null, feature);
		}

		public static void changeContainer(LMObject oldContainer,
										   LMObject newContainer,
										   List<? extends LMObject> children,
										   RawFeature<?, ?> oldFeature,
										   RawFeature<?, ?> newFeature)
		{
			for (final var child : children)
			{
				((FeaturedObject) child).containingFeature = newFeature;
				((FeaturedObject) child).container = newContainer;
			}
			if (oldContainer != null)
			{
				final var oldParentNotification = RelationNotificationBuilder.remove(oldContainer,
																					 oldFeature,
																					 children);
				((FeaturedObject) oldContainer).lNotify(oldParentNotification);
			}
			for (final var child : children)
			{
				final var childNotification = new ContainerChange(child,
																  oldFeature,
																  newFeature,
																  newContainer,
																  oldContainer);
				((FeaturedObject) child).lNotify(childNotification);
			}
			if (newContainer != null)
			{
				final var newParentNotification = RelationNotificationBuilder.insert(newContainer, newFeature, children);
				((FeaturedObject) newContainer).lNotify(newParentNotification);
			}
		}

		public static void changeContainer(LMObject oldContainer,
										   LMObject newContainer,
										   LMObject child,
										   RawFeature<?, ?> oldFeature,
										   RawFeature<?, ?> newFeature)
		{
			if (child != null)
			{
				((FeaturedObject) child).containingFeature = newFeature;
				((FeaturedObject) child).container = newContainer;
			}

			if (oldContainer != null)
			{
				final var oldParentNotification = RelationNotificationBuilder.remove(oldContainer, oldFeature, child);
				((FeaturedObject) oldContainer).lNotify(oldParentNotification);
			}

			if (child != null)
			{
				final var childNotification = new ContainerChange(child,
																  oldFeature,
																  newFeature,
																  newContainer,
																  oldContainer);
				((FeaturedObject) child).lNotify(childNotification);
			}
			if (newContainer != null)
			{
				final var newParentNotification = RelationNotificationBuilder.insert(newContainer, newFeature, child);
				((FeaturedObject) newContainer).lNotify(newParentNotification);
			}
		}
	}
}
