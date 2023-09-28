package org.logoce.lmf.model.util.oldlogoce;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public final class ClassHierarchyIterator implements Iterator<Class<?>>
{
	private final Deque<Class<?>> stack = new ArrayDeque<>();

	public ClassHierarchyIterator(Class<?> classifier)
	{
		loadSuperClasses(classifier);
	}

	private void loadSuperClasses(Class<?> classifier)
	{
		stack.addLast(classifier);

		for (Class<?> iface : classifier.getInterfaces())
		{
			loadInterface(iface);
		}

		if (classifier.getSuperclass() != null)
		{
			loadSuperClasses(classifier.getSuperclass());
		}
	}

	private void loadInterface(Class<?> iface)
	{
		stack.addLast(iface);

		for (Class<?> superIface : iface.getInterfaces())
		{
			loadInterface(superIface);
		}
	}

	@Override
	public boolean hasNext()
	{
		return stack.isEmpty() == false;
	}

	@Override
	public Class<?> next()
	{
		return stack.pop();
	}

}
