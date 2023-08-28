package isotropy.lmf.core.model.notification;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.model.Notification;
import isotropy.lmf.core.model.RawFeature;

import java.util.List;

public class RelationNotificationBuilder
{
	public static final Notification insert(LMObject notifier, RawFeature<?, ?> feature, LMObject newValue)
	{
		if (feature.many())
		{
			return new RelationAddNotifiation(notifier, feature, newValue);
		}
		else
		{
			return new RelationSetNotifiation(notifier, feature, newValue, null);
		}
	}

	public static final Notification insert(LMObject notifier, RawFeature<?, ?> feature, List<? extends LMObject> newValues)
	{
		assert feature.many();
		return new RelationAddManyNotifiation(notifier, feature, newValues);
	}

	public static final Notification remove(LMObject notifier, RawFeature<?, ?> feature, LMObject oldValue)
	{
		if (feature.many())
		{
			return new RelationRemoveNotifiation(notifier, feature, oldValue);
		}
		else
		{
			return new RelationSetNotifiation(notifier, feature, null, oldValue);
		}
	}

	public static final Notification remove(LMObject notifier, RawFeature<?, ?> feature, List<? extends LMObject> oldValues)
	{
		assert feature.many();
		return new RelationRemoveManyNotifiation(notifier, feature, oldValues);
	}
}
