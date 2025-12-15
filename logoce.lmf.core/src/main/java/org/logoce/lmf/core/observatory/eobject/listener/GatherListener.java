package org.logoce.lmf.core.observatory.eobject.listener;

import org.logoce.lmf.core.lang.LMObject;

import java.util.function.Consumer;

public record GatherListener<T extends LMObject>(Consumer<T> discoverObject, Consumer<T> removedObject)
{}
