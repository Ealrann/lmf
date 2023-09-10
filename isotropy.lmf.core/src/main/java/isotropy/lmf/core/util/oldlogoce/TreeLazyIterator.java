package isotropy.lmf.core.util.oldlogoce;

import org.eclipse.emf.common.notify.Notification;
import org.sheepy.lily.core.api.model.ILilyEObject;

import java.util.*;
import java.util.function.Consumer;

public class TreeLazyIterator implements Spliterator<ILilyEObject>
{
	private final Deque<IIterationWrapper> iteratorStack = new ArrayDeque<>();
	private final Deque<IIterationWrapper> oldNodes = new ArrayDeque<>();

	public TreeLazyIterator(final ILilyEObject root)
	{
		final var node = new IterationRoot();
		node.load(root);
		iteratorStack.add(node);
	}

	@Override
	public boolean tryAdvance(final Consumer<? super ILilyEObject> action)
	{
		progress();

		if (iteratorStack.isEmpty() == false)
		{
			final var currentNode = iteratorStack.getLast();
			final var res = currentNode.next();
			action.accept(res);
			iteratorStack.add(newNode(res));
			return true;
		}
		else
		{
			return false;
		}
	}

	private IIterationWrapper newNode(final ILilyEObject element)
	{
		final var res = oldNodes.isEmpty() ? new IterationNode() : oldNodes.pop();
		res.load(element);
		return res;
	}

	private void progress()
	{
		if (iteratorStack.isEmpty() == false)
		{
			final var currentNode = iteratorStack.getLast();
			if (currentNode.hasNext() == false)
			{
				currentNode.dispose();
				oldNodes.add(currentNode);
				iteratorStack.removeLast();
				progress();
			}
		}
	}

	@Override
	public Spliterator<ILilyEObject> trySplit()
	{
		return null;
	}

	@Override
	public long estimateSize()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public int characteristics()
	{
		return CONCURRENT | ORDERED | DISTINCT | NONNULL;
	}

	private interface IIterationWrapper extends Iterator<ILilyEObject>
	{
		void load(final ILilyEObject element);
		void dispose();
	}

	private static final class IterationRoot implements IIterationWrapper
	{
		private ILilyEObject root = null;

		@Override
		public void load(final ILilyEObject element)
		{
			root = element;
		}

		@Override
		public void dispose()
		{
		}

		@Override
		public boolean hasNext()
		{
			return root != null;
		}

		@Override
		public ILilyEObject next()
		{
			final var res = root;
			root = null;
			return res;
		}
	}

	private static final class IterationNode implements IIterationWrapper
	{
		private final Consumer<Notification> listener = this::notifyChanged;
		private final Deque<ILilyEObject> course = new ArrayDeque<>();

		private ElementContext elementContext = null;

		@Override
		public void load(final ILilyEObject element)
		{
			assert course.isEmpty();
			this.elementContext = new ElementContext(element);
			elementContext.load(listener, course);
		}

		@Override
		public void dispose()
		{
			assert course.isEmpty();
			elementContext.dispose(listener);
			elementContext = null;
		}

		@Override
		public boolean hasNext()
		{
			assert elementContext != null;
			return course.isEmpty() == false;
		}

		@Override
		public ILilyEObject next()
		{
			assert elementContext != null;
			return course.pop();
		}

		@SuppressWarnings("unchecked")
		private void notifyChanged(final Notification notification)
		{
			final var newVal = notification.getNewValue();
			final var oldVal = notification.getOldValue();

			switch (notification.getEventType())
			{
				case Notification.ADD, Notification.SET -> {
					if (newVal != null) course.add((ILilyEObject) newVal);
				}
				case Notification.ADD_MANY -> course.addAll((List<ILilyEObject>) newVal);
				case Notification.REMOVE -> {
					if (oldVal != null)
						course.remove((ILilyEObject) oldVal);
				}
				case Notification.REMOVE_MANY -> course.removeAll((List<ILilyEObject>) oldVal);
			}
		}

		private record ElementContext(ILilyEObject element)
		{
			@SuppressWarnings("unchecked")
			public void load(final Consumer<Notification> listener, final Deque<ILilyEObject> course)
			{
				final var eClass = element.eClass();
				for (final var feature : eClass.getEAllContainments())
				{
					final var val = element.eGet(feature);
					if (feature.isMany()) course.addAll((List<ILilyEObject>) val);
					else if (val != null) course.add((ILilyEObject) val);

					element.listen(listener, eClass.getFeatureID(feature));
				}
			}

			public void dispose(Consumer<Notification> listener)
			{
				final var eClass = element.eClass();
				for (final var feature : eClass.getEAllContainments())
				{
					element.sulk(listener, eClass.getFeatureID(feature));
				}
			}
		}
	}
}
