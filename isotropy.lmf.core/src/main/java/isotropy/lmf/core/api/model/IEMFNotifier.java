package isotropy.lmf.core.api.model;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.notification.Notification;

import java.util.List;
import java.util.function.Consumer;

public interface IEMFNotifier
{
	void listen(Consumer<Notification> listener, List<RawFeature<?,?>> features);
	void sulk(Consumer<Notification> listener, List<RawFeature<?,?>> features);

	void listenNoParam(Runnable listener, List<RawFeature<?,?>> features);
	void sulkNoParam(Runnable listener, List<RawFeature<?,?>> features);

	default void listen(Consumer<Notification> listener, RawFeature<?,?> feature)
	{
		listen(listener, List.of(feature));
	}
	default void sulk(Consumer<Notification> listener, RawFeature<?,?> feature)
	{
		sulk(listener, List.of(feature));
	}

	default void listenNoParam(Runnable listener, RawFeature<?,?> feature)
	{
		listenNoParam(listener, List.of(feature));
	}
	default void sulkNoParam(Runnable listener, RawFeature<?,?> feature)
	{
		sulkNoParam(listener, List.of(feature));
	}
}
