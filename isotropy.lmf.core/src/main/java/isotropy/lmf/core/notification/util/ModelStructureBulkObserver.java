package isotropy.lmf.core.notification.util;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.notification.Notification;
import isotropy.lmf.core.lang.LMObject;

import java.util.List;
import java.util.function.Consumer;

public final class ModelStructureBulkObserver
{
	private final ModelObserver observer;
	private final Consumer<List<? extends LMObject>> onAddedObjects;
	private final Consumer<List<? extends LMObject>> onRemovedObjects;

	public ModelStructureBulkObserver(RawFeature<?, ?> relation,
									  Consumer<List<? extends LMObject>> onAddedObjects,
									  Consumer<List<? extends LMObject>> onRemovedObjects)
	{
		this(List.of(relation), onAddedObjects, onRemovedObjects);
	}

	public ModelStructureBulkObserver(List<RawFeature<?, ?>> relations,
									  Consumer<List<? extends LMObject>> onAddedObjects,
									  Consumer<List<? extends LMObject>> onRemovedObjects)
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
