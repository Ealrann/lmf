package org.logoce.lmf.core.notification.observatory.internal.eobject.listener;

import org.logoce.lmf.core.lang.LMObject;

import java.util.function.Consumer;

public record GatherListener<T extends LMObject>(Consumer<T> discoverObject, Consumer<T> removedObject)
{}
