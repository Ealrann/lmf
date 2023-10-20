package org.logoce.lmf.model.util.tree;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public interface NavigableTree<Y extends NavigableTree<Y>>
{
	Stream<Y> streamChildren();

	@SuppressWarnings("unchecked")
	default Stream<Y> streamTree()
	{
		final var us = Stream.of((Y) this);
		final var bellow = streamChildren().flatMap(NavigableTree::streamTree);
		return Stream.concat(us, bellow);
	}

	default void forEach(Consumer<Y> action)
	{
		streamTree().forEach(action);
	}

	/*default <TargetTree extends NavigableTree<TargetTree>> TargetTree map(final BuildFunction<TargetTree> builder)
	{
		return map(null, builder);
	}

	default <TargetTree extends NavigableTree<TargetTree>> TargetTree map(final TargetTree mappedParent,
																		  final BuildFunction<TargetTree> builder)
	{
		return builder.apply(mappedParent,
							 treeParent -> streamChildren().map(c -> c.map(treeParent, builder)).toList());
	}

	@FunctionalInterface
	interface BuildFunction<Y extends NavigableTree<Y>> extends BiFunction<Y, Function<Y, List<Y>>, Y>
	{
		@Override
		Y apply(Y parent, Function<Y, List<Y>> childrenBuilder);
	}*/
}
