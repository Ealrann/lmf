package org.logoce.lmf.core.api.notification.util;

import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.lang.LMObject;
import java.util.List;
import java.util.function.Consumer;

public final class ModelStructureBulkObserver
{
	private final ModelObserver observer;
	private final Consumer<List<? extends LMObject>> onAddedObjects;
	private final Consumer<List<? extends LMObject>> onRemovedObjects;

	public ModelStructureBulkObserver(final int referenceId,
									  final Consumer<List<? extends LMObject>> onAddedObjects,
									  final Consumer<List<? extends LMObject>> onRemovedObjects)
	{
		this(new int[]{referenceId}, onAddedObjects, onRemovedObjects);
	}

	public ModelStructureBulkObserver(final int[] referenceIds,
									  final Consumer<List<? extends LMObject>> onAddedObjects,
									  final Consumer<List<? extends LMObject>> onRemovedObjects)
	{
		this.observer = new ModelObserver(this::notifyChanged, referenceIds);
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
