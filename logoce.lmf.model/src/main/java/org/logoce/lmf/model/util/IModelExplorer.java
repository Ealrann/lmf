package org.logoce.lmf.model.util;

import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.model.lang.LMObject;

import java.util.List;
import java.util.stream.Stream;

public interface IModelExplorer
{
	<T extends LMObject> List<T> explore(LMObject root, Class<T> targetClass);
	List<LMObject> explore(LMObject root);
	<T extends LMObject> Stream<T> stream(LMObject root, Class<T> targetClass);
	Stream<LMObject> stream(LMObject root);
	<T extends IAdapter> List<T> exploreAdapt(LMObject root, Class<T> adapterType);
	<T extends IAdapter> List<T> exploreAdaptGeneric(LMObject root, Class<? extends IAdapter> adapterType);
	<T extends IAdapter> Stream<T> streamAdapt(LMObject root, Class<T> adapterType);
	<T extends IAdapter> Stream<T> streamAdaptGeneric(LMObject root, Class<? extends IAdapter> adapterType);
	<T extends IAdapter> List<T> exploreAdaptNotNull(LMObject root, Class<T> adapterType);
	<T extends IAdapter> Stream<T> streamAdaptNotNull(LMObject root, Class<T> adapterType);
}
