package logoce.lmf.model.notification.observatory;

import logoce.lmf.model.api.notification.Notification;
import logoce.lmf.model.lang.LMObject;

import java.util.List;
import java.util.function.Consumer;

public interface IEObjectObservatoryBuilder<L extends LMObject> extends IStructuralObservatoryBuilder<IEObjectObservatoryBuilder<L>>
{
	IEObjectObservatoryBuilder<L> listenStructure(final Consumer<Notification> structureChanged);
	IEObjectObservatoryBuilder<L> listenStructureNoParam(Runnable structureChanged);
	IEObjectObservatoryBuilder<L> gather(Consumer<L> discoveredObject, Consumer<L> removedObject);
	IEObjectObservatoryBuilder<L> gatherBulk(Consumer<List<L>> discoveredObjects, Consumer<List<L>> removedObjects);
}
