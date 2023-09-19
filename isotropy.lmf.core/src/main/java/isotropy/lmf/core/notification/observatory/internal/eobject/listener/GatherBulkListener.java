package isotropy.lmf.core.notification.observatory.internal.eobject.listener;

import isotropy.lmf.core.lang.LMObject;

import java.util.List;
import java.util.function.Consumer;

public record GatherBulkListener<T extends LMObject>(Consumer<List<T>> discoverObjects,
													 Consumer<List<T>> removedObjects)
{}