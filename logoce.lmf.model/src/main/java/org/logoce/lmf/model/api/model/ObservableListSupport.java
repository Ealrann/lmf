package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Internal helper to translate ObservableList events into model notifications.
 */
final class ObservableListSupport
{
	private ObservableListSupport()
	{
	}

	static <E> BiConsumer<Notification.EventType, List<E>> handler(final FeaturedObject<?> owner,
																   final int featureId,
																   final boolean isRelation,
																   final boolean isContainment)
	{
		return new Handler<>(owner, featureId, isRelation, isContainment);
	}

	private record Handler<E>(FeaturedObject<?> owner,
							  int featureId,
							  boolean relation,
							  boolean containment) implements BiConsumer<Notification.EventType, List<E>>
	{
		@Override
		public void accept(final Notification.EventType eventType, final List<E> elements)
		{
			if (elements.isEmpty()) return;

			if (relation)
			{
				@SuppressWarnings("unchecked") final var children = (List<? extends LMObject>) elements;

				if (containment &&
					(eventType == Notification.EventType.ADD || eventType == Notification.EventType.ADD_MANY))
				{
					if (eventType == Notification.EventType.ADD)
					{
						owner.setContainer(children.getFirst(), featureId);
					}
					else
					{
						owner.setContainer(children, featureId);
					}
				}
			}

			Object newValue = null;
			Object oldValue = null;
			if (!relation)
			{
				switch (eventType)
				{
					case ADD, ADD_MANY -> newValue = elements.size() == 1 ? elements.getFirst() : List.copyOf(elements);
					case REMOVE, REMOVE_MANY ->
							oldValue = elements.size() == 1 ? elements.getFirst() : List.copyOf(elements);
					default ->
					{
						return;
					}
				}
			}
			else
			{
				@SuppressWarnings("unchecked") final var children = (List<? extends LMObject>) elements;
				switch (eventType)
				{
					case ADD -> newValue = children.getFirst();
					case ADD_MANY -> newValue = children;
					case REMOVE -> oldValue = children.getFirst();
					case REMOVE_MANY ->
					{
						oldValue = children;
						newValue = List.of();
					}
					default ->
					{
						return;
					}
				}
			}

			if (containment) owner.beforeContainmentNotify(eventType, oldValue, newValue);
			owner.notifier().notify(featureId, containment, true, eventType, oldValue, newValue);
			if (containment) owner.afterContainmentNotify(eventType, oldValue, newValue);
		}
	}
}
