package org.logoce.lmf.model.notification.util;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

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
			container.listen(containerListener, source.lmContainingFeature());
		}

		public void uninstall()
		{
			final var container = source.lmContainer();
			if (container != null)
			{
				container.sulk(containerListener, source.lmContainingFeature());
			}
		}

		private void containerChange(Notification notification)
		{
			final var oldParent = notification.notifier();
			final var newParent = source.lmContainer();

			if (oldParent != newParent)
			{
				final var oldContainingFeature = notification.feature();
				oldParent.sulk(containerListener, oldContainingFeature);
				if (newParent != null)
				{
					newParent.listen(containerListener, source.lmContainingFeature());
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
