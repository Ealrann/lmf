package org.logoce.lmf.core.notification;

import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.lang.LMObject;

import java.util.List;

public record RelationRemoveManyNotifiation(LMObject notifier,
											boolean isContainment,
											int featureId,
											List<? extends LMObject> oldValues) implements Notification
{
	@Override
	public int featureId()
	{
		return featureId;
	}

	@Override
	public Object newValue()
	{
		return List.of();
	}

	@Override
	public Object oldValue()
	{
		return oldValues;
	}

	@Override
	public EventType type()
	{
		return EventType.REMOVE_MANY;
	}
}
