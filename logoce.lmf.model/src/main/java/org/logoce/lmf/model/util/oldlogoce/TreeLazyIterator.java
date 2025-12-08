package org.logoce.lmf.model.util.oldlogoce;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.util.ModelUtil;

import java.util.*;
import java.util.function.Consumer;

public class TreeLazyIterator implements Spliterator<LMObject>
{
	private final Deque<IIterationWrapper> iteratorStack = new ArrayDeque<>();
	private final Deque<IIterationWrapper> oldNodes = new ArrayDeque<>();

	public TreeLazyIterator(final LMObject root)
	{
		final var node = new IterationRoot();
		node.load(root);
		iteratorStack.add(node);
	}

	@Override
	public boolean tryAdvance(final Consumer<? super LMObject> action)
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

	private IIterationWrapper newNode(final LMObject element)
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
	public Spliterator<LMObject> trySplit()
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

	private interface IIterationWrapper extends Iterator<LMObject>
	{
		void load(final LMObject element);
		void dispose();
	}

	private static final class IterationRoot implements IIterationWrapper
	{
		private LMObject root = null;

		@Override
		public void load(final LMObject element)
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
		public LMObject next()
		{
			final var res = root;
			root = null;
			return res;
		}
	}

	private static final class IterationNode implements IIterationWrapper
	{
		private final Consumer<Notification> listener = this::notifyChanged;
		private final Deque<LMObject> course = new ArrayDeque<>();

		private ElementContext elementContext = null;

		@Override
		public void load(final LMObject element)
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
		public LMObject next()
		{
			assert elementContext != null;
			return course.pop();
		}

		@SuppressWarnings("unchecked")
		private void notifyChanged(final Notification notification)
		{
			final var newVal = notification.newValue();
			final var oldVal = notification.oldValue();

			switch (notification.type())
			{
				case ADD, SET ->
				{
					if (newVal != null) course.add((LMObject) newVal);
				}
				case ADD_MANY -> course.addAll((List<LMObject>) newVal);
				case REMOVE ->
				{
					if (oldVal != null) course.remove((LMObject) oldVal);
				}
				case REMOVE_MANY -> course.removeAll((List<LMObject>) oldVal);
			}
		}

		private record ElementContext(LMObject element)
		{
			@SuppressWarnings("unchecked")
			public void load(final Consumer<Notification> listener, final Deque<LMObject> course)
			{
				final var group = element.lmGroup();
				ModelUtil.streamContainmentFeatures(group).forEach(feature -> {
					final var val = element.get(feature.featureSupplier().get());
					if (feature.many()) course.addAll((List<LMObject>) val);
					else if (val != null) course.add((LMObject) val);
					element.listen(listener, feature);
				});
			}

			public void dispose(Consumer<Notification> listener)
			{
				final var group = element.lmGroup();
				ModelUtil.streamContainmentFeatures(group).forEach(feature -> {
					element.sulk(listener, feature);
				});
			}
		}
	}
}
