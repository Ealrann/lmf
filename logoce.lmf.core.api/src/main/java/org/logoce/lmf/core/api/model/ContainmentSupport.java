package org.logoce.lmf.core.api.model;

import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Relation;
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

		if (oldContainer == newContainer && oldFeatureId == newFeatureId)
		{
			return;
		}

		if (oldContainer != null && oldFeatureId != -1)
		{
			final var oldContainment = resolveContainmentRelation(oldContainer, oldFeatureId);
			if (oldContainment != null && oldContainment.immutable() && !(child instanceof Feature<?, ?, ?, ?>))
			{
				throw cannotMoveFromImmutableContainment(oldContainer, oldContainment, child);
			}
			detachFromOldContainer(oldContainer, oldFeatureId, child);
		}

		featuredChild.container = newContainer;
		featuredChild.containingFeatureId = newFeatureId;

		featuredChild.notifier().notify(newFeatureId,
										true,
										false,
										Notification.EventType.CONTAINER,
										oldContainer,
										newContainer);
	}

	private static Relation<?, ?, ?, ?> resolveContainmentRelation(final LMObject container, final int featureId)
	{
		final var group = container.lmGroup();
		if (group == null)
		{
			return null;
		}

		final int featureIndex;
		try
		{
			featureIndex = container.featureIndex(featureId);
		}
		catch (final RuntimeException exception)
		{
			return null;
		}

		if (featureIndex < 0 || featureIndex >= group.features().size())
		{
			return null;
		}

		final var feature = group.features().get(featureIndex);
		if (feature instanceof Relation<?, ?, ?, ?> relation && relation.contains())
		{
			return relation;
		}

		return null;
	}

	private static IllegalStateException cannotMoveFromImmutableContainment(final LMObject container,
																		   final Relation<?, ?, ?, ?> relation,
																		   final LMObject target)
	{
		return new IllegalStateException("Cannot move object [" +
										 target.lmGroup().name() +
										 "] because it is contained by immutable relation [" +
										 container.lmGroup().name() +
										 "." +
										 relation.name() +
										 "]");
	}

	private static void detachFromOldContainer(final LMObject oldContainer, final int oldFeatureId, final LMObject child)
	{
		final var group = oldContainer.lmGroup();
		if (group == null)
		{
			return;
		}

		final var featureIndex = oldContainer.featureIndex(oldFeatureId);
		if (featureIndex < 0 || featureIndex >= group.features().size())
		{
			return;
		}

		final var feature = group.features().get(featureIndex);
		if (!(feature instanceof Relation<?, ?, ?, ?> relation) || !relation.contains())
		{
			return;
		}
		if (relation.immutable())
		{
			return;
		}

		final var currentValue = oldContainer.get(oldFeatureId);
		if (relation.many())
		{
			if (!(currentValue instanceof List<?> list) || list.isEmpty())
			{
				return;
			}

			boolean found = false;
			for (final var element : list)
			{
				if (element == child)
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				return;
			}

			list.removeIf(o -> o == child);
		}
		else
		{
			if (currentValue != child)
			{
				return;
			}
			oldContainer.set(oldFeatureId, null);
		}
	}
}
