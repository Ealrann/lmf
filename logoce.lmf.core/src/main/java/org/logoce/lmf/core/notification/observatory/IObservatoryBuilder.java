package org.logoce.lmf.core.notification.observatory;

import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.notification.observatory.internal.eobject.RootObservatory;
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
