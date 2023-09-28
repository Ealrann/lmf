package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

import java.util.List;

public record RelationRemoveManyNotifiation(LMObject notifier,
											RawFeature<?, ?> feature,
											List<? extends LMObject> oldValues) implements Notification
{
	@Override
	public Object newValue()
	{
		return List.of();
	}

	@Override
	public Object oldValue()
	{
		return oldValues;
	}

	@Override
	public EventType type()
	{
		return EventType.REMOVE_MANY;
	}
}
