package isotropy.lmf.core.notification.impl;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.api.notification.Notification;
import isotropy.lmf.core.api.feature.RawFeature;

public record SetNotifiation(LMObject notifier, RawFeature<?, ?> feature, Object newValue, Object oldValue) implements
																											Notification
{
	@Override
	public EventType type()
	{
		return feature().relation() && newValue == null ? EventType.UNSET : EventType.SET;
	}
}
