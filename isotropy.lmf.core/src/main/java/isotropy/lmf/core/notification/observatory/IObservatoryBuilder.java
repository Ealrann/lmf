package isotropy.lmf.core.notification.observatory;

import org.logoce.notification.api.IFeatures;
import org.logoce.notification.api.INotifier;
import org.sheepy.lily.core.api.model.ILilyEObject;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.RootObservatory;

public interface IObservatoryBuilder extends IEObjectObservatoryBuilder<ILilyEObject>
{
	static IObservatoryBuilder newObservatoryBuilder()
	{
		return new RootObservatory.Builder();
	}

	IObservatoryBuilder focus(ILilyEObject focus);
	<Y extends IFeatures<Y>> INotifierObservatoryBuilder<Y> focus(INotifier<Y> notifier);

	IObservatory build();

	boolean isEmpty();
}
