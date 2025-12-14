package org.logoce.lmf.core.notification.impl;

import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;

public record SetNotification(LMObject notifier,
							  boolean isContainment,
							  int featureId,
							  Object newValue,
							  Object oldValue) implements Notification
{
	@Override
	public int featureId()
	{
		return featureId;
	}

	@Override
	public EventType type()
	{
		return feature() instanceof Relation<?, ?, ?, ?> && newValue == null ? EventType.UNSET : EventType.SET;
	}
}
