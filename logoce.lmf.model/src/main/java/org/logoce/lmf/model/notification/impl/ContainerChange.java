package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

public record ContainerChange(LMObject notifier,
							  int oldFeatureId,
							  int newFeatureId,
							  Object newContainer,
							  Object oldContainer) implements Notification
{
	@Override
	public int featureId()
	{
		return newFeatureId;
	}

	@Override
	public EventType type()
	{
		return EventType.CONTAINER;
	}

	@Override
	public Object newValue()
	{
		return newContainer;
	}

	@Override
	public Object oldValue()
	{
		return oldContainer;
	}
}
