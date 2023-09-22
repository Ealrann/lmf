package logoce.lmf.model.notification.observatory.internal.eobject.listener;

import logoce.lmf.model.lang.LMObject;

import java.util.List;
import java.util.function.Consumer;

public record GatherBulkListener<T extends LMObject>(Consumer<List<T>> discoverObjects,
													 Consumer<List<T>> removedObjects)
{}