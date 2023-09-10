package isotropy.lmf.core.notification;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.api.notification.Notification;
import isotropy.lmf.core.api.feature.RawFeature;

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
