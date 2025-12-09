package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;

public record SetNotifiation(LMObject notifier, Feature<?, ?> feature, Object newValue,
							 Object oldValue) implements Notification
{
	@Override
	public EventType type()
	{
		return feature() instanceof Relation<?, ?> && newValue == null ? EventType.UNSET : EventType.SET;
	}
}
