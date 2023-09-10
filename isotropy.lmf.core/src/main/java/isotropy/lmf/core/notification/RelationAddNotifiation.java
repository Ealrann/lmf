package isotropy.lmf.core.notification;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.api.notification.Notification;
import isotropy.lmf.core.api.feature.RawFeature;

public record RelationAddNotifiation(LMObject notifier, RawFeature<?, ?> feature, LMObject newValue) implements
																									 Notification
{
	@Override
	public EventType type()
	{
		return EventType.ADD;
	}
}
