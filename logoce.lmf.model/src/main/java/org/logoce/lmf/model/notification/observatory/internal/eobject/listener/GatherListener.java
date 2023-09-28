package org.logoce.lmf.model.notification.observatory.internal.eobject.listener;

import org.logoce.lmf.model.lang.LMObject;

import java.util.function.Consumer;

public record GatherListener<T extends LMObject>(Consumer<T> discoverObject, Consumer<T> removedObject)
{}
