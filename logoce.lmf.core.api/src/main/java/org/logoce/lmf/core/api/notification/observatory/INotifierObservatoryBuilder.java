package org.logoce.lmf.core.api.notification.observatory;

import org.logoce.lmf.core.api.notification.IFeature;
import org.logoce.lmf.core.api.notification.IFeatures;

public interface INotifierObservatoryBuilder<Type extends IFeatures<?>>
{
	<Listener> INotifierObservatoryBuilder<Type> listen(Listener listener, IFeature<Listener, ? super Type> feature);
	INotifierObservatoryBuilder<Type> listenNoParam(Runnable listener, IFeature<?, ? super Type> feature);
}
