package isotropy.lmf.core.notification.observatory;

import org.logoce.extender.api.IAdapter;
import org.logoce.notification.api.IFeatures;
import org.logoce.notification.api.INotifier;

public interface INotifierAdapterObservatoryBuilder<Type extends IFeatures<?>, Notifier extends IAdapter & INotifier<?>> extends
																														  INotifierObservatoryBuilder<Type>,
																														  IAdapterObservatoryBuilder<Notifier>
{
}
