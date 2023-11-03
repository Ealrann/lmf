package org.logoce.lmf.model.util.tree;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AbstractLazyMappedTree<Y extends AbstractLazyMappedTree<Y>> implements StructuredTree<Y>
{
	private final Mapper<?, Y> mapper;

	private Y parent = null;
	private List<Y> children = null;

	public <InputNode extends StructuredTree<InputNode>> AbstractLazyMappedTree(final InputNode inputNode,
																				final Function<InputNode, Y> mapper)
	{
		this.mapper = new Mapper<>(inputNode, mapper);
	}

	@Override
	public Y parent()
	{
		if (parent == null) parent = mapper.mapParent();
		return parent;
	}

	public List<Y> children()
	{
		if (children == null) children = mapper.mapChildren();
		return children;
	}

	@Override
	public Stream<Y> streamChildren()
	{
		if (children == null) children = mapper.mapChildren();
		return children.stream();
	}

	private record Mapper<InputNode extends StructuredTree<InputNode>, OutputNode extends AbstractLazyMappedTree<OutputNode>>(
			InputNode inputNode,
			Function<InputNode, OutputNode> mapper)
	{
		public OutputNode mapParent()
		{
			final var parent = inputNode.parent();
			return parent != null ? mapper.apply(parent) : null;
		}

		public List<OutputNode> mapChildren()
		{
			return inputNode.streamChildren().map(mapper).toList();
		}
	}
}
