package isotropy.lmf.core.notification.observatory;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.notification.observatory.internal.eobject.RootObservatory;
import org.logoce.notification.api.IFeatures;
import org.logoce.notification.api.INotifier;

public interface IObservatoryBuilder extends IEObjectObservatoryBuilder<LMObject>
{
	static IObservatoryBuilder newObservatoryBuilder()
	{
		return new RootObservatory.Builder();
	}

	IObservatoryBuilder focus(LMObject focus);
	<Y extends IFeatures<Y>> INotifierObservatoryBuilder<Y> focus(INotifier<Y> notifier);

	IObservatory build();

	boolean isEmpty();
}
