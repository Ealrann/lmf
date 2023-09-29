package org.logoce.lmf.model.util;

import java.util.List;
import java.util.function.Function;

public final class Tree<T> extends AbstractTree<T, Tree<T>>
{
	public Tree(final Tree<T> parent, final T data, final Function<Tree<T>, List<Tree<T>>> childrenBuilder)
	{
		super(parent, data, childrenBuilder);
	}

	public Tree(BuildInfo<T, Tree<T>> info)
	{
		super(info);
	}

	public <Target> Tree<Target> map(final Function<Tree<T>, Target> mapper)
	{
		return super.map(mapper, Tree::new);
	}
}
