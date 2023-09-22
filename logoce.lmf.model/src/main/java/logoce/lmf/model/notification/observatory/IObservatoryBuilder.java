package logoce.lmf.model.notification.observatory;

import logoce.lmf.model.lang.LMObject;
import logoce.lmf.model.notification.observatory.internal.eobject.RootObservatory;
import org.logoce.lmf.notification.api.IFeatures;
import org.logoce.lmf.notification.api.INotifier;

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
