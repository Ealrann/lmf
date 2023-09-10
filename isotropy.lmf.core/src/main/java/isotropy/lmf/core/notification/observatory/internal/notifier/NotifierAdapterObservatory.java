package isotropy.lmf.core.notification.observatory.internal.notifier;

import org.logoce.extender.api.IAdapter;
import org.logoce.notification.api.Feature;
import org.logoce.notification.api.IFeatures;
import org.logoce.notification.api.INotifier;
import org.sheepy.lily.core.api.notification.observatory.INotifierAdapterObservatoryBuilder;
import org.sheepy.lily.core.api.notification.observatory.IObservatory;
import org.sheepy.lily.core.api.notification.observatory.internal.InternalObservatoryBuilder;
import org.sheepy.lily.core.api.notification.observatory.internal.allocation.AdapterObservatory;
import org.sheepy.lily.core.api.notification.observatory.internal.allocation.IAdapterPOI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class NotifierAdapterObservatory<Type extends IFeatures<?>, Notifier extends IAdapter & INotifier<? extends Type>> extends
																																 AdapterObservatory<Notifier> implements
																																							  IObservatory
{
	private final List<INotifierPOI<Type>> observationPoints;

	public NotifierAdapterObservatory(final Class<Notifier> notifierAdapterClass,
									  final List<IAdapterPOI<Notifier>> listeners,
									  final List<INotifierPOI<Type>> observationPoints,
									  final List<Consumer<Notifier>> addListeners,
									  final List<Consumer<Notifier>> removeListeners)
	{
		super(notifierAdapterClass, listeners, addListeners, removeListeners);
		this.observationPoints = List.copyOf(observationPoints);
	}

	@Override
	protected void gatherAdd(final Notifier newAdapter)
	{
		super.gatherAdd(newAdapter);
		for (final var point : observationPoints)
		{
			point.listen(newAdapter);
		}
	}

	@Override
	protected void gatherRemove(final Notifier oldAdapter)
	{
		for (final var point : observationPoints)
		{
			point.sulk(oldAdapter);
		}
		super.gatherRemove(oldAdapter);
	}

	public static final class Builder<Type extends IFeatures<?>, Notifier extends IAdapter & INotifier<? extends Type>> extends
																														 AdapterObservatory.Builder<Notifier> implements
																																							  INotifierAdapterObservatoryBuilder<Type, Notifier>,
																																							  InternalObservatoryBuilder
	{
		private final List<INotifierPOI<Type>> observationPoints = new ArrayList<>();

		public Builder(Class<Notifier> notifierAdapterClass)
		{
			super(notifierAdapterClass);
		}

		@Override
		public <Listener> INotifierAdapterObservatoryBuilder<Type, Notifier> listen(Listener listener,
																					Feature<Listener, ? super Type> feature)
		{
			observationPoints.add(new NotifierPOI<>(listener, feature));
			return this;
		}

		@Override
		public INotifierAdapterObservatoryBuilder<Type, Notifier> listenNoParam(final Runnable listener,
																				final Feature<?, ? super Type> feature)
		{
			observationPoints.add(new NoParamNotifierPOI<>(listener, feature));
			return this;
		}

		@Override
		public IObservatory build()
		{
			return new NotifierAdapterObservatory<>(adapterClass,
													listeners,
													observationPoints,
													addListeners,
													removeListeners);
		}
	}
}
