package logoce.lmf.model.notification.impl;

import logoce.lmf.model.api.feature.RawFeature;
import logoce.lmf.model.api.notification.Notification;
import logoce.lmf.model.lang.LMObject;

import java.util.List;

public record RelationAddManyNotifiation(LMObject notifier, RawFeature<?, ?> feature,
										 List<? extends LMObject> newValues) implements Notification
{
	@Override
	public Object newValue()
	{
		return newValues;
	}

	@Override
	public EventType type()
	{
		return EventType.ADD_MANY;
	}
}
