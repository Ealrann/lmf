package isotropy.lmf.core.model.notification;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.model.Notification;
import isotropy.lmf.core.model.RawFeature;

public record RelationAddNotifiation(LMObject notifier, RawFeature<?, ?> feature, LMObject newValue) implements
																									 Notification
{
	@Override
	public EventType type()
	{
		return EventType.ADD;
	}
}
