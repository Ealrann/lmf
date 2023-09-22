package logoce.lmf.model.notification.impl;

import logoce.lmf.model.api.feature.RawFeature;
import logoce.lmf.model.api.notification.Notification;
import logoce.lmf.model.lang.LMObject;

public record ContainerChange(LMObject notifier,
							  RawFeature<?, ?> oldContainingFeature,
							  RawFeature<?, ?> newContainingFeature,
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
	public RawFeature<?, ?> feature()
	{
		return newContainingFeature;
	}
}
