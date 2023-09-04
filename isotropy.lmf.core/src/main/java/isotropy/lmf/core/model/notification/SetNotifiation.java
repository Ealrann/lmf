package isotropy.lmf.core.model.notification;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.model.Notification;
import isotropy.lmf.core.model.RawFeature;

public record SetNotifiation(LMObject notifier,
							 RawFeature<?, ?> feature,
							 Object newValue,
							 Object oldValue) implements Notification
{
	@Override
	public EventType type()
	{
		return feature().relation() && newValue == null ? EventType.UNSET : EventType.SET;
	}
}
