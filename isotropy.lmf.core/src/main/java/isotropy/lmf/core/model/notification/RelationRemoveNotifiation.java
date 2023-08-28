package isotropy.lmf.core.model.notification;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.model.Notification;
import isotropy.lmf.core.model.RawFeature;

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
