package logoce.lmf.model.notification.impl;

import logoce.lmf.model.api.feature.RawFeature;
import logoce.lmf.model.api.notification.Notification;
import logoce.lmf.model.lang.LMObject;

public record SetNotifiation(LMObject notifier, RawFeature<?, ?> feature, Object newValue, Object oldValue) implements
																											Notification
{
	@Override
	public EventType type()
	{
		return feature().relation() && newValue == null ? EventType.UNSET : EventType.SET;
	}
}
