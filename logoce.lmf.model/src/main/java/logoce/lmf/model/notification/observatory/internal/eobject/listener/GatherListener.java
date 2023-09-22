package logoce.lmf.model.notification.observatory.internal.eobject.listener;

import logoce.lmf.model.lang.LMObject;

import java.util.function.Consumer;

public record GatherListener<T extends LMObject>(Consumer<T> discoverObject, Consumer<T> removedObject)
{}
