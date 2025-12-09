package org.logoce.lmf.model.notification.util;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;

import java.util.List;
import java.util.function.Consumer;

public final class ModelStructureObserver
{
	private final ModelObserver observer;
	private final Consumer<LMObject> onAddedObject;
	private final Consumer<LMObject> onRemovedObject;

	public ModelStructureObserver(final Relation<?, ?> feature,
								  final Consumer<LMObject> onAddedObject,
								  final Consumer<LMObject> onRemovedObject)
	{
		this(List.of(feature), onAddedObject, onRemovedObject);
	}

	public ModelStructureObserver(final List<Relation<?, ?>> features,
								  final Consumer<LMObject> onAddedObject,
								  final Consumer<LMObject> onRemovedObject)
	{
		this.observer = new ModelObserver(this::notifyChanged, features);
		this.onAddedObject = onAddedObject;
		this.onRemovedObject = onRemovedObject;
	}

	public void startObserve(LMObject root)
	{
		observer.startObserve(root);
	}

	public void stopObserve(LMObject root)
	{
		observer.stopObserve(root);
	}

	private void notifyChanged(Notification notification)
	{
		NotificationUnifier.unify(notification, onAddedObject, onRemovedObject);
	}
}
