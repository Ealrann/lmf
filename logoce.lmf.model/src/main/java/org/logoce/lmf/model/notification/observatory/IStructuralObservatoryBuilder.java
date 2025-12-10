package org.logoce.lmf.model.notification.observatory;

import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.notification.api.IFeatures;
import org.logoce.lmf.notification.api.INotifier;

import java.util.function.Consumer;

public interface IStructuralObservatoryBuilder<C extends IStructuralObservatoryBuilder<C>>
{
	IEObjectObservatoryBuilder<LMObject> explore(int referenceId);
	<Target extends LMObject> IEObjectObservatoryBuilder<Target> explore(int referenceId, Class<Target> cast);
	IEObjectObservatoryBuilder<LMObject> exploreParent();
	<Target extends LMObject> IEObjectObservatoryBuilder<Target> exploreParent(Class<Target> cast);

	<T extends IAdapter> IAdapterObservatoryBuilder<T> adapt(Class<T> classifier);
	<F extends IFeatures<?>, T extends IAdapter & INotifier<? extends F>> INotifierAdapterObservatoryBuilder<F, T> adaptNotifier(
			Class<T> classifier);

	C listen(Consumer<Notification> listener, int... features);
	C listenNoParam(Runnable listener, int... features);
}
