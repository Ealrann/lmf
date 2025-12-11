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
									  boolean isContainment,
									  final boolean isMany,
									  final LMObject newValue)
	{
		if (isMany)
		{
			return new RelationAddNotifiation(notifier, isContainment, featureId, newValue);
		}
		else
		{
			return new SetNotification(notifier, isContainment, featureId, newValue, null);
		}
	}

	public static Notification insert(final LMObject notifier,
									  final int featureId,
									  boolean isContainment,
									  final List<? extends LMObject> newValues)
	{
		return new RelationAddManyNotifiation(notifier, isContainment, featureId, newValues);
	}

	public static Notification remove(final LMObject notifier,
									  final int featureId,
									  boolean isContainment,
									  final boolean isMany,
									  final LMObject oldValue)
	{
		if (isMany)
		{
			return new RelationRemoveNotifiation(notifier, isContainment, featureId, oldValue);
		}
		else
		{
			return new SetNotification(notifier, isContainment, featureId, null, oldValue);
		}
	}

	public static Notification remove(final LMObject notifier,
									  final int featureId,
									  boolean isContainment,
									  final List<? extends LMObject> oldValues)
	{
		return new RelationRemoveManyNotifiation(notifier, isContainment, featureId, oldValues);
	}
}
