package org.logoce.lmf.model.notification.util;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;

import java.util.List;
import java.util.function.Consumer;

public final class ModelStructureBulkObserver
{
	private final ModelObserver observer;
	private final Consumer<List<? extends LMObject>> onAddedObjects;
	private final Consumer<List<? extends LMObject>> onRemovedObjects;

	public ModelStructureBulkObserver(final Relation<?, ?> relation,
									  final Consumer<List<? extends LMObject>> onAddedObjects,
									  final Consumer<List<? extends LMObject>> onRemovedObjects)
	{
		this(List.of(relation), onAddedObjects, onRemovedObjects);
	}

	public ModelStructureBulkObserver(final List<Relation<?, ?>> relations,
									  final Consumer<List<? extends LMObject>> onAddedObjects,
									  final Consumer<List<? extends LMObject>> onRemovedObjects)
	{
		this.observer = new ModelObserver(this::notifyChanged, relations);
		this.onAddedObjects = onAddedObjects;
		this.onRemovedObjects = onRemovedObjects;
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
		NotificationUnifier.unifyList(notification, onAddedObjects, onRemovedObjects);
	}
}
