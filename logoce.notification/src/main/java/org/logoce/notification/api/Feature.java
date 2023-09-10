package org.logoce.notification.api;

public final class Feature<Callback, Type extends IFeatures<?>>
{
	public static <Callback, Type extends IFeatures<?>> Feature<Callback, Type> newFeature()
	{
		//noinspection InstantiationOfUtilityClass
		return new Feature<>();
	}
}
