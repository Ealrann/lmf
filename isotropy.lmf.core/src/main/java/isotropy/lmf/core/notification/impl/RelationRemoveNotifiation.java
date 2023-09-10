package isotropy.lmf.core.notification.impl;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.api.notification.Notification;
import isotropy.lmf.core.api.feature.RawFeature;

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
