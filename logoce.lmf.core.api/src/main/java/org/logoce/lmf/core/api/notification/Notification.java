package org.logoce.lmf.core.api.notification;

import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.LMObject;

import java.util.List;

public interface Notification
{
	LMObject notifier();
	default Feature<?, ?, ?, ?> feature()
	{
		final var notifier = notifier();
		final int featureIdx = notifier.featureIndex(featureId());
		final var group = notifier.lmGroup();
		return group.features().get(featureIdx);
	}
	boolean isContainment();
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
