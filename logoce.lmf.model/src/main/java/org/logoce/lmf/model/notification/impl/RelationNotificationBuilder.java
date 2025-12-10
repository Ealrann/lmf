package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

import java.util.List;

public final class RelationNotificationBuilder
{
	private RelationNotificationBuilder()
	{
	}

	public static Notification insert(final LMObject notifier,
									  final int featureId,
									  final boolean isMany,
									  final LMObject newValue)
	{
		if (isMany)
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
		return new RelationAddManyNotifiation(notifier, featureId, newValues);
	}

	public static Notification remove(final LMObject notifier,
									  final int featureId,
									  final boolean isMany,
									  final LMObject oldValue)
	{
		if (isMany)
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
		return new RelationRemoveManyNotifiation(notifier, featureId, oldValues);
	}
}
