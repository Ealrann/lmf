package org.logoce.lmf.model.util.tree;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractDataTree<T, Y extends AbstractDataTree<T, Y>> extends AbstractTree<Y> implements DataNode<T>,
																											   StructuredTree<Y>
{
	protected final T data;

	public AbstractDataTree(final Y parent, final T data, final Function<Y, List<Y>> childrenBuilder)
	{
		super(parent, childrenBuilder);
		this.data = data;
	}

	public AbstractDataTree(final NavigableDataTree.BuildInfo<T, Y> info)
	{
		this(info.parent(), info.data(), info.childrenBuilder());
	}

	@Override
	public T data()
	{
		return data;
	}
}
