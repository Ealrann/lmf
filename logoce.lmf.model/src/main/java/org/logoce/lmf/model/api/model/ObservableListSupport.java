package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.notification.impl.RelationNotificationBuilder;
import org.logoce.lmf.model.notification.impl.SetNotification;

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

			final Notification notification;

			if (!relation)
			{
				Object newValue = null;
				Object oldValue = null;

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

				notification = new SetNotification((LMObject) owner, containment, featureId, newValue, oldValue);
			}
			else
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

				notification = switch (eventType)
				{
					case ADD -> RelationNotificationBuilder.insert((LMObject) owner,
																   featureId,
																   containment,
																   true,
																   children.getFirst());
					case ADD_MANY ->
							RelationNotificationBuilder.insert((LMObject) owner, featureId, containment, children);
					case REMOVE -> RelationNotificationBuilder.remove((LMObject) owner,
																	  featureId,
																	  containment,
																	  true,
																	  children.getFirst());
					case REMOVE_MANY ->
							RelationNotificationBuilder.remove((LMObject) owner, featureId, containment, children);
					default -> null;
				};

				if (notification == null) return;
			}

			owner.eNotify(notification);
		}
	}
}
