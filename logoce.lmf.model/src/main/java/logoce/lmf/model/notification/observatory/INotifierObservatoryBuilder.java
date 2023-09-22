package logoce.lmf.model.notification.observatory;

import org.logoce.lmf.notification.api.IFeature;
import org.logoce.lmf.notification.api.IFeatures;

public interface INotifierObservatoryBuilder<Type extends IFeatures<?>>
{
	<Listener> INotifierObservatoryBuilder<Type> listen(Listener listener, IFeature<Listener, ? super Type> feature);
	INotifierObservatoryBuilder<Type> listenNoParam(Runnable listener, IFeature<?, ? super Type> feature);
}
