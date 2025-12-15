package org.logoce.lmf.core.notification;

import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.lang.LMObject;

public record RelationRemoveNotifiation(LMObject notifier,
										boolean isContainment,
										int featureId,
										LMObject oldValue) implements Notification
{
	@Override
	public int featureId()
	{
		return featureId;
	}

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
