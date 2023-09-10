package org.logoce.notification.api;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public interface INotifier<Type extends IFeatures<?>>
{
	<Callback> void listen(Callback listener, Feature<? super Callback, ? super Type> feature);
	<Callback> void listen(Callback listener, Collection<? extends Feature<? super Callback, ? super Type>> features);
	void listenNoParam(Runnable listener, Feature<?, ? super Type> feature);
	void listenNoParam(Runnable listener, Collection<? extends Feature<?, ? super Type>> features);

	<Callback> void sulk(Callback listener, Feature<? super Callback, ? super Type> feature);
	<Callback> void sulk(Callback listener, Collection<? extends Feature<? super Callback, ? super Type>> features);
	void sulkNoParam(Runnable listener, Feature<?, ? super Type> feature);
	void sulkNoParam(Runnable listener, Collection<? extends Feature<?, ? super Type>> features);

	interface Internal<Type extends IFeatures<?>> extends INotifier<Type>
	{
		<T> void notify(Feature<Consumer<T>, ? super Type> feature, T value);
		void notify(Feature<IntConsumer, ? super Type> feature, int value);
		void notify(Feature<LongConsumer, ? super Type> feature, long value);
		void notify(Feature<BooleanConsumer, ? super Type> feature, boolean value);
		void notify(Feature<Runnable, ? super Type> feature);

		<Callback> void notify(Feature<? super Callback, ? super Type> feature, Consumer<Callback> caller);
	}
}
