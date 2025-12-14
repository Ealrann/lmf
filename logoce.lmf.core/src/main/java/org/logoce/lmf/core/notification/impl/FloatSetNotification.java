package org.logoce.lmf.core.notification.impl;

import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.lang.LMObject;

public record FloatSetNotification(LMObject notifier,
								   boolean isContainment,
								   int featureId,
								   float newFloatValue,
								   float oldFloatValue) implements Notification
{
	@Override
	public EventType type()
	{
		return EventType.SET;
	}

	@Override
	public Object newValue()
	{
		return newFloatValue;
	}

	@Override
	public Object oldValue()
	{
		return oldFloatValue;
	}

	@Override
	public float floatValue()
	{
		return newFloatValue;
	}
}

