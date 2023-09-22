package logoce.lmf.model.notification.impl;

import logoce.lmf.model.api.feature.RawFeature;
import logoce.lmf.model.api.notification.Notification;
import logoce.lmf.model.lang.LMObject;

public record RelationAddNotifiation(LMObject notifier, RawFeature<?, ?> feature, LMObject newValue) implements
																									 Notification
{
	@Override
	public EventType type()
	{
		return EventType.ADD;
	}
}
