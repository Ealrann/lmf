package isotropy.lmf.core.notification.observatory;

import org.logoce.notification.api.Feature;
import org.logoce.notification.api.IFeatures;

public interface INotifierObservatoryBuilder<Type extends IFeatures<?>>
{
	<Listener> INotifierObservatoryBuilder<Type> listen(Listener listener, Feature<Listener, ? super Type> feature);
	INotifierObservatoryBuilder<Type> listenNoParam(Runnable listener, Feature<?, ? super Type> feature);
}
