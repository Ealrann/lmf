package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

public record IntSetNotification(LMObject notifier,
								 boolean isContainment,
								 int featureId,
								 int newIntValue,
								 int oldIntValue) implements Notification
{
	@Override
	public EventType type()
	{
		return EventType.SET;
	}

	@Override
	public Object newValue()
	{
		return newIntValue;
	}

	@Override
	public Object oldValue()
	{
		return oldIntValue;
	}

	@Override
	public int intValue()
	{
		return newIntValue;
	}
}

