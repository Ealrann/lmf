package org.logoce.lmf.model.notification.impl;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

public record SetNotifiation(LMObject notifier, RawFeature<?, ?> feature, Object newValue, Object oldValue) implements
																											Notification
{
	@Override
	public EventType type()
	{
		return feature().relation() && newValue == null ? EventType.UNSET : EventType.SET;
	}
}
