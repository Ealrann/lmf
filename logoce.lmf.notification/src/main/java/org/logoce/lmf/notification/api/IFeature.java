package org.logoce.lmf.notification.api;

public interface IFeature<Callback, Type extends IFeatures<?>>
{
	static <Callback, Type extends IFeatures<?>> IFeature<Callback, Type> newFeature()
	{
		return new SimpleFeature<>();
	}

	record SimpleFeature<Callback, Type extends IFeatures<?>>() implements IFeature<Callback, Type> {}
}
