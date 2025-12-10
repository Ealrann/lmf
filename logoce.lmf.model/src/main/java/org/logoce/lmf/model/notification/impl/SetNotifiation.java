package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;

public record SetNotifiation(LMObject notifier, int featureId, Object newValue,
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
