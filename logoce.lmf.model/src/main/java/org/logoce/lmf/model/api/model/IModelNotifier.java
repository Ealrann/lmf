package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.notification.listener.IModelListener;
import org.logoce.lmf.notification.api.IFeatures;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface IModelNotifier<Type extends IFeatures<?>>
{
	<Callback extends IModelListener> void listen(Callback listener, Feature<?, ?, ? super Callback, ? super Type> feature);
	<Callback extends IModelListener> void listen(Callback listener,
							Collection<? extends Feature<?, ?, ? super Callback, ? super Type>> features);

	<Callback extends IModelListener> void sulk(Callback listener, Feature<?, ?, ? super Callback, ? super Type> feature);
	<Callback extends IModelListener> void sulk(Callback listener,
						  Collection<? extends Feature<?, ?, ? super Callback, ? super Type>> features);

	void listen(Consumer<Notification> listener, int... featureIDs);
	void sulk(Consumer<Notification> listener, int... featureIDs);

	void listenNoParam(Runnable listener, int... featureIDs);
	void sulkNoParam(Runnable listener, int... featureIDs);

	void listen(Consumer<Notification> listener, List<Feature<?, ?, ?, ?>> features);
	void sulk(Consumer<Notification> listener, List<Feature<?, ?, ?, ?>> features);

	void listenNoParam(Runnable listener, List<Feature<?, ?, ?, ?>> features);
	void sulkNoParam(Runnable listener, List<Feature<?, ?, ?, ?>> features);

	void listenStructure(Consumer<Notification> listener);
	void sulkStructure(Consumer<Notification> listener);

	void listenStructureNoParam(Runnable listener);
	void sulkStructureNoParam(Runnable listener);

	default void listen(Consumer<Notification> listener, Feature<?, ?, ?, ?> feature)
	{
		listen(listener, feature.id());
	}
	default void sulk(Consumer<Notification> listener, Feature<?, ?, ?, ?> feature)
	{
		sulk(listener, feature.id());
	}

	default void listenNoParam(Runnable listener, Feature<?, ?, ?, ?> feature)
	{
		listenNoParam(listener, feature.id());
	}
	default void sulkNoParam(Runnable listener, Feature<?, ?, ?, ?> feature)
	{
		sulkNoParam(listener, feature.id());
	}

	interface Impl<Type extends IFeatures<?>> extends IModelNotifier<Type>
	{
		void notify(Notification notification);
		boolean eDeliver();
		void eDeliver(boolean deliver);
	}
}
