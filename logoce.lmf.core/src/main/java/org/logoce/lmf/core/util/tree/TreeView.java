package org.logoce.lmf.core.util.tree;

import java.util.function.Function;
import java.util.stream.Stream;

public final class TreeView<T> implements BasicTree<T, TreeView<T>>
{
	private final T data;
	private final Function<T, Stream<T>> childrenExplorer;
	private final Function<T, T> parentExplorer;

	public TreeView(T data, Function<T, Stream<T>> childrenExplorer, Function<T, T> parentExplorer)
	{
		assert data != null;
		this.data = data;
		this.childrenExplorer = childrenExplorer;
		this.parentExplorer = parentExplorer;
	}

	@Override
	public T data()
	{
		return data;
	}

	@Override
	public TreeView<T> parent()
	{
		final var parent = parentExplorer.apply(data);
		return parent != null ? new TreeView<>(parent, childrenExplorer, parentExplorer) : null;
	}

	@Override
	public Stream<TreeView<T>> streamChildren()
	{
		return childrenExplorer.apply(data).map(this::newChild);
	}

	private TreeView<T> newChild(T childData)
	{
		return new TreeView<>(childData, childrenExplorer, parentExplorer);
	}
}
