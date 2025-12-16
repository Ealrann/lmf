package org.logoce.lmf.core.api.notification.util;

import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.lang.LMObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ParentObserver
{
	private final ParentListener listener;
	private final List<DedicatedListener> installedListeners = new ArrayList<>(1);

	public ParentObserver(ParentListener listener)
	{
		this.listener = listener;
	}

	public void startObserve(LMObject source)
	{
		final var listener = new DedicatedListener(source);
		listener.install(source);
		installedListeners.add(listener);
	}

	public void stopObserve(LMObject source)
	{
		final var installedListener = installedListeners.stream()
														.filter(listener -> listener.source == source)
														.findAny();
		assert installedListener.isPresent();
		final var listener = installedListener.get();
		listener.uninstall();
		installedListeners.remove(listener);
	}

	private final class DedicatedListener
	{
		private final LMObject source;
		private final Consumer<Notification> containerListener = this::containerChange;

		private DedicatedListener(LMObject source)
		{
			this.source = source;
		}

		public void install(LMObject source)
		{
			final var container = source.lmContainer();
			assert container !=
				   null; // cannot deal with this case. Please stop listen parent before removing an object.
			container.notifier().listen(containerListener, source.lmContainingFeatureID());
		}

		public void uninstall()
		{
			final var container = source.lmContainer();
			if (container != null)
			{
				container.notifier().sulk(containerListener, source.lmContainingFeatureID());
			}
		}

		private void containerChange(Notification notification)
		{
			final var oldParent = notification.notifier();
			final var newParent = source.lmContainer();

			if (oldParent != newParent)
			{
				final int oldContainingFeatureId = notification.featureId();
				oldParent.notifier().sulk(containerListener, oldContainingFeatureId);
				if (newParent != null)
				{
					newParent.notifier().listen(containerListener, source.lmContainingFeatureID());
				}
				listener.accept(oldParent, newParent);
			}
		}
	}

	@FunctionalInterface
	public interface ParentListener extends BiConsumer<LMObject, LMObject>
	{
		void parentChanged(LMObject oldParent, LMObject newParent);

		@Override
		default void accept(LMObject oldParent, LMObject newParent)
		{
			parentChanged(oldParent, newParent);
		}
	}
}
