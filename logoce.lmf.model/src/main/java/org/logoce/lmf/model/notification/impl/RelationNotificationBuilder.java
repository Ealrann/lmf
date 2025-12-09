package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;

import java.util.List;

public class RelationNotificationBuilder
{
	private RelationNotificationBuilder()
	{
	}

	public static Notification insert(final LMObject notifier, final Feature<?, ?> feature, final LMObject newValue)
	{
		if (feature instanceof Relation<?, ?> relation && relation.many())
		{
			return new RelationAddNotifiation(notifier, feature, newValue);
		}
		else
		{
			return new SetNotifiation(notifier, feature, newValue, null);
		}
	}

	public static Notification insert(final LMObject notifier,
									  final Feature<?, ?> feature,
									  final List<? extends LMObject> newValues)
	{
		if (!(feature instanceof Relation<?, ?> relation) || !relation.many())
		{
			throw new IllegalArgumentException("RelationAddMany requires a many-valued relation feature");
		}
		return new RelationAddManyNotifiation(notifier, feature, newValues);
	}

	public static Notification remove(final LMObject notifier,
									  final Feature<?, ?> feature,
									  final LMObject oldValue)
	{
		if (feature instanceof Relation<?, ?> relation && relation.many())
		{
			return new RelationRemoveNotifiation(notifier, feature, oldValue);
		}
		else
		{
			return new SetNotifiation(notifier, feature, null, oldValue);
		}
	}

	public static Notification remove(final LMObject notifier,
									  final Feature<?, ?> feature,
									  final List<? extends LMObject> oldValues)
	{
		if (!(feature instanceof Relation<?, ?> relation) || !relation.many())
		{
			throw new IllegalArgumentException("RelationRemoveMany requires a many-valued relation feature");
		}
		return new RelationRemoveManyNotifiation(notifier, feature, oldValues);
	}
}

