package org.logoce.lmf.core.notification.observatory;

import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.lang.LMObject;

import java.util.List;
import java.util.function.Consumer;

public interface IEObjectObservatoryBuilder<L extends LMObject> extends IStructuralObservatoryBuilder<IEObjectObservatoryBuilder<L>>
{
	IEObjectObservatoryBuilder<L> listenStructure(final Consumer<Notification> structureChanged);
	IEObjectObservatoryBuilder<L> listenStructureNoParam(Runnable structureChanged);
	IEObjectObservatoryBuilder<L> gather(Consumer<L> discoveredObject, Consumer<L> removedObject);
	IEObjectObservatoryBuilder<L> gatherBulk(Consumer<List<L>> discoveredObjects, Consumer<List<L>> removedObjects);
}
