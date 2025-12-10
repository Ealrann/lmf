package org.logoce.lmf.model.api.notification;

import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.LMObject;

import java.util.List;

public interface Notification
{
	LMObject notifier();
	default Feature<?, ?> feature()
	{
		final var notifier = notifier();
		if (notifier == null) return null;

		final var group = notifier.lmGroup();
		for (final var feature : group.features())
		{
			if (feature.id() == featureId())
			{
				return feature;
			}
		}

		throw new IllegalStateException("Unknown featureId " + featureId() + " for group " + group.name());
	}
	int featureId();
	EventType type();
	default Object oldValue() {return null;}
	Object newValue();
	default List<?> newValues() {return List.of();}
	default List<?> oldValues() {return List.of();}
	default int intValue() {return 0;}
	default float floatValue() {return 0F;}
	default double doubleValue() {return 0.;}
	default boolean booleanValue() {return false;}
	default long longValue() {return 0L;}

	enum EventType
	{
		ADD,
		REMOVE,
		ADD_MANY,
		REMOVE_MANY,
		SET,
		UNSET,
		CONTAINER;
	}
}
