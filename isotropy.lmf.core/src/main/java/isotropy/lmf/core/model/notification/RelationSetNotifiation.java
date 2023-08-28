package isotropy.lmf.core.model.notification;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.model.Notification;
import isotropy.lmf.core.model.RawFeature;

public record RelationSetNotifiation(LMObject notifier,
									 RawFeature<?, ?> feature,
									 LMObject newValue,
									 LMObject oldValue) implements Notification
{
	@Override
	public EventType type()
	{
		return newValue == null ? EventType.UNSET : EventType.SET;
	}
}
