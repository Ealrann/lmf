package org.logoce.lmf.model.notification.util;

import org.logoce.lmf.model.lang.LMObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModelSetRegistry<T extends LMObject>
{
	private final Class<T> classCast;
	private final List<T> list = new ArrayList<>();
	private final ModelStructureObserver structureObserver;

	public ModelSetRegistry(final Class<T> classCast, final int[] features)
	{
		structureObserver = new ModelStructureObserver(features, this::add, this::remove);
		this.classCast = classCast;
	}

	public void startRegister(LMObject root)
	{
		structureObserver.startObserve(root);
	}

	public void stopRegister(LMObject root)
	{
		structureObserver.stopObserve(root);
	}

	public List<T> getElements()
	{
		return Collections.unmodifiableList(list);
	}

	private void add(LMObject newValue)
	{
		list.add(classCast.cast(newValue));
	}

	private void remove(LMObject oldValue)
	{
		list.remove(classCast.cast(oldValue));
	}
}
