package isotropy.lmf.core.notification;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.api.notification.Notification;
import isotropy.lmf.core.api.feature.RawFeature;

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
