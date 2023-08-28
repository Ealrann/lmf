package isotropy.lmf.core.model.notification;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.model.Notification;
import isotropy.lmf.core.model.RawFeature;

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
