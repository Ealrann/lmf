package org.logoce.lmf.model.util.tree;

import java.util.List;
import java.util.function.Function;

public sealed interface NavigableDataTree<T, Y extends NavigableDataTree<T, Y>> extends NavigableTree<Y>,
																						DataNode<T> permits Tree,
																											TreeView
{
	default <Target, TargetTree extends StructuredTree<TargetTree>> TargetTree map(final Function<Y, Target> dataMapper,
																							  final Function<BuildInfo<Target, TargetTree>, TargetTree> builder)
	{
		return map(null, dataMapper, builder);
	}

	@SuppressWarnings("unchecked")
	default <Target, TargetTree extends StructuredTree<TargetTree>> TargetTree map(final TargetTree mappedParent,
																							  final Function<Y, Target> dataMapper,
																							  final Function<BuildInfo<Target, TargetTree>, TargetTree> builder)
	{
		final var buildInfo = new BuildInfo<>(mappedParent,
											  dataMapper.apply((Y) this),
											  treeParent -> streamChildren().map(c -> c.map(treeParent,
																							dataMapper,
																							builder)).toList());
		return builder.apply(buildInfo);
	}

	record BuildInfo<T, Y>(Y parent, T data, Function<Y, List<Y>> childrenBuilder) {}
}
