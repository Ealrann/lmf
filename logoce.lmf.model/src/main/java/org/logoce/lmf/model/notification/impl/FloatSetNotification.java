package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

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

