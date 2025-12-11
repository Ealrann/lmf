package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

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
