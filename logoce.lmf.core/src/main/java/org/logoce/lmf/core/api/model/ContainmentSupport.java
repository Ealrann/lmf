package org.logoce.lmf.core.api.model;

import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.lang.LMObject;

import java.util.List;

/**
 * Internal helper for containment/backpointer management.
 * Package-private on purpose; not part of the public API.
 */
final class ContainmentSupport
{
	private ContainmentSupport()
	{
	}

	static void setContainer(final LMObject newContainer, final LMObject child, final int featureId)
	{
		setContainerInternal(newContainer, child, featureId);
	}

	static void setContainer(final LMObject newContainer,
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

		if (oldContainer != null && oldFeatureId != -1 && oldContainer.lmGroup() != null)
		{
			final var oldParent = (FeaturedObject<?>) oldContainer;
			oldParent.beforeContainmentNotify(Notification.EventType.REMOVE, child, null);
			oldParent.notifier().notify(oldFeatureId, true, true, Notification.EventType.REMOVE, child, null);
			oldParent.afterContainmentNotify(Notification.EventType.REMOVE, child, null);
		}

		featuredChild.notifier().notify(newFeatureId,
										true,
										false,
										Notification.EventType.CONTAINER,
										oldContainer,
										newContainer);
	}
}
