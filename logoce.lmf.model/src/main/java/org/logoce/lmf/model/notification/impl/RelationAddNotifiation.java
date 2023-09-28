package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

public record RelationAddNotifiation(LMObject notifier, RawFeature<?, ?> feature, LMObject newValue) implements
																									 Notification
{
	@Override
	public EventType type()
	{
		return EventType.ADD;
	}
}
