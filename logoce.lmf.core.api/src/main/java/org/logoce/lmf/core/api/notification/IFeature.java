package org.logoce.lmf.core.api.notification;

public interface IFeature<Callback, Type extends IFeatures<?>>
{
	static <Callback, Type extends IFeatures<?>> IFeature<Callback, Type> newFeature()
	{
		return new SimpleFeature<>();
	}

	final class SimpleFeature<Callback, Type extends IFeatures<?>> implements IFeature<Callback, Type> {}
}
