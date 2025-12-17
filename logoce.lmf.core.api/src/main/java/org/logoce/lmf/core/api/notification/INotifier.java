package org.logoce.lmf.core.api.notification;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public interface INotifier<Type extends IFeatures<?>>
{
	<Callback> void listen(Callback listener, IFeature<? super Callback, ? super Type> feature);
	<Callback> void listen(Callback listener, Collection<? extends IFeature<? super Callback, ? super Type>> features);
	void listenNoParam(Runnable listener, IFeature<?, ? super Type> feature);
	void listenNoParam(Runnable listener, Collection<? extends IFeature<?, ? super Type>> features);

	<Callback> void sulk(Callback listener, IFeature<? super Callback, ? super Type> feature);
	<Callback> void sulk(Callback listener, Collection<? extends IFeature<? super Callback, ? super Type>> features);
	void sulkNoParam(Runnable listener, IFeature<?, ? super Type> feature);
	void sulkNoParam(Runnable listener, Collection<? extends IFeature<?, ? super Type>> features);

	interface Internal<Type extends IFeatures<?>> extends INotifier<Type>
	{
		<T> void notify(IFeature<Consumer<T>, ? super Type> feature, T value);
		void notify(IFeature<IntConsumer, ? super Type> feature, int value);
		void notify(IFeature<LongConsumer, ? super Type> feature, long value);
		void notify(IFeature<BooleanConsumer, ? super Type> feature, boolean value);
		void notify(IFeature<Runnable, ? super Type> feature);

		<Callback> void notify(IFeature<? super Callback, ? super Type> feature, Consumer<Callback> caller);
	}
}
