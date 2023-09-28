package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

public record RelationRemoveNotifiation(LMObject notifier, RawFeature<?, ?> feature, LMObject oldValue) implements
																										Notification
{
	@Override
	public Object newValue()
	{
		return null;
	}

	@Override
	public EventType type()
	{
		return EventType.REMOVE;
	}
}
