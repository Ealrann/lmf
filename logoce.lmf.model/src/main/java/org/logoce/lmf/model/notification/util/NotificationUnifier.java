package org.logoce.lmf.model.notification.util;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;

import java.util.List;
import java.util.function.Consumer;

public final class NotificationUnifier
{
	public static void unify(Notification notification, Consumer<LMObject> onAdd, Consumer<LMObject> onRemove)
	{
		unifyAdded(notification, onAdd);
		unifyRemoved(notification, onRemove);
	}

	@SuppressWarnings("unchecked")
	public static void unifyAdded(Notification notification, Consumer<LMObject> onAdd)
	{
		switch (notification.type())
		{
			case SET, UNSET ->
			{
				final var setted = notification.newValue();
				final var unsetted = notification.oldValue();
				if (setted != unsetted)
				{
					if (setted instanceof LMObject settedLMObject)
					{
						onAdd.accept(settedLMObject);
					}
				}
			}
			case ADD -> onAdd.accept((LMObject) notification.newValue());
			case ADD_MANY ->
			{
				final var newList = (List<LMObject>) notification.newValue();
				for (int i = 0; i < newList.size(); i++)
				{
					onAdd.accept(newList.get(i));
				}
			}
			default -> {}
		}
	}

	@SuppressWarnings("unchecked")
	public static void unifyRemoved(Notification notification, Consumer<LMObject> onRemove)
	{
		switch (notification.type())
		{
			case SET, UNSET ->
			{
				final var setted = notification.newValue();
				final var unsetted = notification.oldValue();
				if (setted != unsetted)
				{
					if (unsetted instanceof LMObject)
					{
						onRemove.accept((LMObject) unsetted);
					}
				}
			}
			case REMOVE -> onRemove.accept((LMObject) notification.oldValue());
			case REMOVE_MANY ->
			{
				final var oldList = (List<LMObject>) notification.oldValue();
				for (int i = 0; i < oldList.size(); i++)
				{
					onRemove.accept(oldList.get(i));
				}
			}
			default -> {}
		}
	}

	@SuppressWarnings("unchecked")
	public static void unifyList(Notification notification,
								 Consumer<List<? extends LMObject>> onAdd,
								 Consumer<List<? extends LMObject>> onRemove)
	{
		assert notification.feature() instanceof Relation<?, ?, ?, ?>;
		switch (notification.type())
		{
			case SET, UNSET ->
			{
				final var setted = (LMObject) notification.newValue();
				final var unsetted = (LMObject) notification.oldValue();
				if (setted != unsetted)
				{
					if (unsetted != null)
					{
						onRemove.accept(List.of(unsetted));
					}
					if (setted != null)
					{
						onAdd.accept(List.of(setted));
					}
				}
			}
			case ADD -> onAdd.accept(List.of((LMObject) notification.newValue()));
			case ADD_MANY -> onAdd.accept((List<LMObject>) notification.newValue());
			case REMOVE -> onRemove.accept(List.of((LMObject) notification.oldValue()));
			case REMOVE_MANY -> onRemove.accept((List<LMObject>) notification.oldValue());
			default -> {}
		}
	}
}
