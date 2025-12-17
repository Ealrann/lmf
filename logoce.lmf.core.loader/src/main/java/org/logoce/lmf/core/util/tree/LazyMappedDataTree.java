package org.logoce.lmf.core.util.tree;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public final class LazyMappedDataTree<T> implements BasicTree<T, LazyMappedDataTree<T>>
{
	private final Mapper<T, ?, ?> mapper;

	private T data = null;
	private LazyMappedDataTree<T> parent = null;
	private List<LazyMappedDataTree<T>> children = null;

	public <InputType, InputNode extends BasicTree<InputType, InputNode>> LazyMappedDataTree(final InputNode inputNode,
																							 final Function<InputType, T> mapper)
	{
		assert inputNode != null;
		this.mapper = new Mapper<>(inputNode, mapper);
	}

	@Override
	public T data()
	{
		if (data == null) data = mapper.mapData();
		return data;
	}

	@Override
	public LazyMappedDataTree<T> parent()
	{
		if (parent == null) parent = mapper.mapParent();
		return parent;
	}

	public List<LazyMappedDataTree<T>> children()
	{
		if (children == null) children = mapper.mapChildren();
		return children;
	}

	@Override
	public Stream<LazyMappedDataTree<T>> streamChildren()
	{
		if (children == null) children = mapper.mapChildren();
		return children.stream();
	}

	private record Mapper<T, O, I extends BasicTree<O, I>>(I inputNode, Function<O, T> mapper)
	{
		public T mapData()
		{
			return mapper.apply(inputNode.data());
		}

		public LazyMappedDataTree<T> mapParent()
		{
			final var parent = inputNode.parent();
			return parent != null ? new LazyMappedDataTree<>(parent, mapper) : null;
		}

		public List<LazyMappedDataTree<T>> mapChildren()
		{
			return inputNode.streamChildren().map(this::mapChild).toList();
		}

		private LazyMappedDataTree<T> mapChild(I child)
		{
			return new LazyMappedDataTree<>(child, mapper);
		}
	}
}
