package org.logoce.lmf.model.util.tree;

import java.util.function.Function;
import java.util.stream.Stream;

public final class TreeView<T> implements NavigableDataTree<T, TreeView<T>>
{
	private final T data;
	private final Function<T, Stream<T>> childrenExplorer;

	public TreeView(T data, Function<T, Stream<T>> childrenExplorer)
	{
		this.data = data;
		this.childrenExplorer = childrenExplorer;
	}

	@Override
	public T data()
	{
		return data;
	}

	@Override
	public Stream<TreeView<T>> streamChildren()
	{
		return childrenExplorer.apply(data).map(this::newChild);
	}

	private TreeView<T> newChild(T childData)
	{
		return new TreeView<>(childData, childrenExplorer);
	}
}
