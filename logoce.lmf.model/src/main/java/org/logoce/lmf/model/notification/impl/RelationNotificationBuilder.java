package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;

import java.util.List;

public class RelationNotificationBuilder
{
	private RelationNotificationBuilder()
	{
	}

	public static Notification insert(final LMObject notifier, final int featureId, final LMObject newValue)
	{
		final var feature = resolveFeature(notifier, featureId);
		if (feature instanceof Relation<?, ?> relation && relation.many())
		{
			return new RelationAddNotifiation(notifier, featureId, newValue);
		}
		else
		{
			return new SetNotifiation(notifier, featureId, newValue, null);
		}
	}

	public static Notification insert(final LMObject notifier,
									  final int featureId,
									  final List<? extends LMObject> newValues)
	{
		final var feature = resolveFeature(notifier, featureId);
		if (!(feature instanceof Relation<?, ?> relation) || !relation.many())
		{
			throw new IllegalArgumentException("RelationAddMany requires a many-valued relation feature");
		}
		return new RelationAddManyNotifiation(notifier, featureId, newValues);
	}

	public static Notification remove(final LMObject notifier,
									  final int featureId,
									  final LMObject oldValue)
	{
		final var feature = resolveFeature(notifier, featureId);
		if (feature instanceof Relation<?, ?> relation && relation.many())
		{
			return new RelationRemoveNotifiation(notifier, featureId, oldValue);
		}
		else
		{
			return new SetNotifiation(notifier, featureId, null, oldValue);
		}
	}

	public static Notification remove(final LMObject notifier,
									  final int featureId,
									  final List<? extends LMObject> oldValues)
	{
		final var feature = resolveFeature(notifier, featureId);
		if (!(feature instanceof Relation<?, ?> relation) || !relation.many())
		{
			throw new IllegalArgumentException("RelationRemoveMany requires a many-valued relation feature");
		}
		return new RelationRemoveManyNotifiation(notifier, featureId, oldValues);
	}

	private static org.logoce.lmf.model.lang.Feature<?, ?> resolveFeature(final LMObject notifier,
																		  final int featureId)
	{
		final var group = notifier.lmGroup();
		for (final var feature : group.features())
		{
			if (feature.id() == featureId)
			{
				return feature;
			}
		}
		throw new IllegalArgumentException("Unknown featureId " + featureId + " for group " + group.name());
	}
}
