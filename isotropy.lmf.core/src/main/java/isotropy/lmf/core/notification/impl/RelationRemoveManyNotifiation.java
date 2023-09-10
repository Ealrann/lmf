package isotropy.lmf.core.notification.impl;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.api.notification.Notification;
import isotropy.lmf.core.api.feature.RawFeature;

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
