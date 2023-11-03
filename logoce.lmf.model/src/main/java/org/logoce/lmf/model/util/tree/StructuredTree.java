package org.logoce.lmf.model.util.tree;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface StructuredTree<Y extends StructuredTree<Y>>
{
	Y parent();
	Stream<Y> streamChildren();

	@SuppressWarnings("unchecked")
	default Y root()
	{
		final var parent = parent();
		return parent == null ? (Y) this : parent.root();
	}

	@SuppressWarnings("unchecked")
	default Stream<Y> streamTree()
	{
		final var us = Stream.of((Y) this);
		final var bellow = streamChildren().flatMap(StructuredTree::streamTree);
		return Stream.concat(us, bellow);
	}

	default void forEach(Consumer<Y> action)
	{
		streamTree().forEach(action);
	}
}
