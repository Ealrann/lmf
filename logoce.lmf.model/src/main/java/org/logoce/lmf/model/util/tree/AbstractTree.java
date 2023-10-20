package org.logoce.lmf.model.util.tree;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AbstractTree<Y extends StructuredTree<Y>> implements StructuredTree<Y>
{
	protected final Y parent;
	protected final List<Y> children;

	public AbstractTree(final Y parent)
	{
		this.parent = parent;
		this.children = List.of();
	}

	@SuppressWarnings("unchecked")
	public AbstractTree(final Y parent, final Function<Y, List<Y>> childrenBuilder)
	{
		this.parent = parent;
		this.children = List.copyOf(childrenBuilder.apply((Y) this));
	}

	@Override
	public final Y parent()
	{
		return parent;
	}

	@Override
	public final List<Y> children()
	{
		return children;
	}

	public final Stream<Y> streamChildren()
	{
		return children.stream();
	}
}
