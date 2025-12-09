package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.LMObject;

public record ContainerChange(LMObject notifier,
							  Feature<?, ?> oldContainingFeature,
							  Feature<?, ?> newContainingFeature,
							  Object newContainer,
							  Object oldContainer) implements Notification
{
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

	@Override
	public Feature<?, ?> feature()
	{
		return newContainingFeature;
	}
}
