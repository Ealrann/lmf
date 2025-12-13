package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

public record BooleanSetNotification(LMObject notifier,
									 boolean isContainment,
									 int featureId,
									 boolean newBooleanValue,
									 boolean oldBooleanValue) implements Notification
{
	@Override
	public EventType type()
	{
		return EventType.SET;
	}

	@Override
	public Object newValue()
	{
		return newBooleanValue;
	}

	@Override
	public Object oldValue()
	{
		return oldBooleanValue;
	}

	@Override
	public boolean booleanValue()
	{
		return newBooleanValue;
	}
}

