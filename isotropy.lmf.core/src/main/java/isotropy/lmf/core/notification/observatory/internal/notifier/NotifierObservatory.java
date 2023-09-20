package isotropy.lmf.core.notification.observatory.internal.notifier;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.notification.observatory.INotifierObservatoryBuilder;
import isotropy.lmf.core.notification.observatory.IObservatory;
import isotropy.lmf.core.notification.observatory.internal.InternalObservatoryBuilder;
import org.logoce.notification.api.IFeature;
import org.logoce.notification.api.IFeatures;
import org.logoce.notification.api.INotifier;

import java.util.ArrayList;
import java.util.List;

public final class NotifierObservatory<Type extends IFeatures<? extends Type>> implements IObservatory
{
	private final INotifier<? extends Type> notifier;
	private final List<INotifierPOI<Type>> observationPoints;

	public NotifierObservatory(final INotifier<? extends Type> notifier, List<INotifierPOI<Type>> observationPoints)
	{
		this.notifier = notifier;
		this.observationPoints = List.copyOf(observationPoints);
	}

	@Override
	public void observe(LMObject object)
	{
		for (final var point : observationPoints)
		{
			point.listen(notifier);
		}
	}

	@Override
	public void shut(LMObject object)
	{
		for (final var point : observationPoints)
		{
			point.sulk(notifier);
		}
	}

	public static final class Builder<Type extends IFeatures<Type>> implements INotifierObservatoryBuilder<Type>,
																			   InternalObservatoryBuilder
	{
		private final INotifier<Type> notifier;
		private final List<INotifierPOI<Type>> observationPoints = new ArrayList<>();

		public Builder(INotifier<Type> notifier)
		{
			this.notifier = notifier;
		}

		@Override
		public <Listener> INotifierObservatoryBuilder<Type> listen(Listener listener,
																   IFeature<Listener, ? super Type> feature)
		{
			observationPoints.add(new NotifierPOI<>(listener, feature));
			return this;
		}

		@Override
		public INotifierObservatoryBuilder<Type> listenNoParam(final Runnable listener,
															   final IFeature<?, ? super Type> feature)
		{
			observationPoints.add(new NoParamNotifierPOI<>(listener, feature));
			return this;
		}

		@Override
		public IObservatory build()
		{
			return new NotifierObservatory<>(notifier, observationPoints);
		}
	}
}
