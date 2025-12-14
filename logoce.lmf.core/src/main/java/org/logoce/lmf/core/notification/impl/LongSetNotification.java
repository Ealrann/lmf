package org.logoce.lmf.core.notification.impl;

import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.lang.LMObject;

public record LongSetNotification(LMObject notifier,
								  boolean isContainment,
								  int featureId,
								  long newLongValue,
								  long oldLongValue) implements Notification
{
	@Override
	public EventType type()
	{
		return EventType.SET;
	}

	@Override
	public Object newValue()
	{
		return newLongValue;
	}

	@Override
	public Object oldValue()
	{
		return oldLongValue;
	}

	@Override
	public long longValue()
	{
		return newLongValue;
	}
}

