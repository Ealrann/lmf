package org.logoce.lmf.core.api.notification.observatory;

import org.logoce.lmf.core.api.extender.IAdapter;
import org.logoce.lmf.core.api.notification.IFeatures;
import org.logoce.lmf.core.api.notification.INotifier;

public interface INotifierAdapterObservatoryBuilder<Type extends IFeatures<?>, Notifier extends IAdapter & INotifier<?>> extends
																														  INotifierObservatoryBuilder<Type>,
																														  IAdapterObservatoryBuilder<Notifier>
{
}
