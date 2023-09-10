package isotropy.lmf.core.api.model;

import org.eclipse.emf.common.notify.Notification;

import java.util.function.Consumer;

public interface IEMFNotifier
{
	void listen(Consumer<Notification> listener, int... features);
	void sulk(Consumer<Notification> listener, int... features);

	void listenNoParam(Runnable listener, int... features);
	void sulkNoParam(Runnable listener, int... features);
}
