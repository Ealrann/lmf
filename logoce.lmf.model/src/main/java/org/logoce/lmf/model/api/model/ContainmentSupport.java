package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.notification.impl.ContainerChange;
import org.logoce.lmf.model.notification.impl.RelationNotificationBuilder;

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
			final var oldParentNotification = RelationNotificationBuilder.remove(oldContainer,
																				oldFeatureId,
																				true,
																				true,
																				child);
			((FeaturedObject<?>) oldContainer).eNotify(oldParentNotification);
		}

		final var childNotification = new ContainerChange(child,
														 oldFeatureId,
														 newFeatureId,
														 newContainer,
														 oldContainer);
		featuredChild.eNotify(childNotification);
	}
}

