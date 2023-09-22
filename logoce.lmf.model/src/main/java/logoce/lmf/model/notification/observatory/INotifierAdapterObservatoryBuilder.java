package logoce.lmf.model.notification.observatory;

import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.notification.api.IFeatures;
import org.logoce.lmf.notification.api.INotifier;

public interface INotifierAdapterObservatoryBuilder<Type extends IFeatures<?>, Notifier extends IAdapter & INotifier<?>> extends
																														  INotifierObservatoryBuilder<Type>,
																														  IAdapterObservatoryBuilder<Notifier>
{
}
