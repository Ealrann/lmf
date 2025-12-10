package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

import java.util.List;

public record RelationAddManyNotifiation(LMObject notifier, int featureId,
										 List<? extends LMObject> newValues) implements Notification
{
	@Override
	public int featureId()
	{
		return featureId;
	}

	@Override
	public Object newValue()
	{
		return newValues;
	}

	@Override
	public EventType type()
	{
		return EventType.ADD_MANY;
	}
}
