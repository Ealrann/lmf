package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

public record RelationRemoveNotifiation(LMObject notifier,
										boolean isContainment,
										int featureId,
										LMObject oldValue) implements Notification
{
	@Override
	public int featureId()
	{
		return featureId;
	}

	@Override
	public Object newValue()
	{
		return null;
	}

	@Override
	public EventType type()
	{
		return EventType.REMOVE;
	}
}
