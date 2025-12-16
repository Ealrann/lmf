package org.logoce.lmf.core.notification;

import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.lang.LMObject;

public record RelationAddNotifiation(LMObject notifier,
									 boolean isContainment,
									 int featureId,
									 LMObject newValue) implements Notification
{
	@Override
	public int featureId()
	{
		return featureId;
	}

	@Override
	public EventType type()
	{
		return EventType.ADD;
	}
}
