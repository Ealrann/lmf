package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

public record DoubleSetNotification(LMObject notifier,
									boolean isContainment,
									int featureId,
									double newDoubleValue,
									double oldDoubleValue) implements Notification
{
	@Override
	public EventType type()
	{
		return EventType.SET;
	}

	@Override
	public Object newValue()
	{
		return newDoubleValue;
	}

	@Override
	public Object oldValue()
	{
		return oldDoubleValue;
	}

	@Override
	public double doubleValue()
	{
		return newDoubleValue;
	}
}

