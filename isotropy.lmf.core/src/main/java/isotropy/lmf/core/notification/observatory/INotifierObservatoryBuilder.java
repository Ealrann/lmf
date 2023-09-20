package isotropy.lmf.core.notification.observatory;

import org.logoce.notification.api.IFeature;
import org.logoce.notification.api.IFeatures;

public interface INotifierObservatoryBuilder<Type extends IFeatures<?>>
{
	<Listener> INotifierObservatoryBuilder<Type> listen(Listener listener, IFeature<Listener, ? super Type> feature);
	INotifierObservatoryBuilder<Type> listenNoParam(Runnable listener, IFeature<?, ? super Type> feature);
}
