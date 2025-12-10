package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.Feature;

import java.util.List;
import java.util.function.Consumer;

public interface IEMFNotifier
{
	void listen(Consumer<Notification> listener, int... featureIDs);
	void sulk(Consumer<Notification> listener, int... featureIDs);

	void listenNoParam(Runnable listener, int... featureIDs);
	void sulkNoParam(Runnable listener, int... featureIDs);


	void listen(Consumer<Notification> listener, List<Feature<?, ?>> features);
	void sulk(Consumer<Notification> listener, List<Feature<?, ?>> features);

	void listenNoParam(Runnable listener, List<Feature<?, ?>> features);
	void sulkNoParam(Runnable listener, List<Feature<?, ?>> features);

	default void listen(Consumer<Notification> listener, Feature<?, ?> feature)
	{
		listen(listener, feature.id());
	}
	default void sulk(Consumer<Notification> listener, Feature<?, ?> feature)
	{
		sulk(listener, feature.id());
	}

	default void listenNoParam(Runnable listener, Feature<?, ?> feature)
	{
		listenNoParam(listener, feature.id());
	}
	default void sulkNoParam(Runnable listener, Feature<?, ?> feature)
	{
		sulkNoParam(listener, feature.id());
	}
}
