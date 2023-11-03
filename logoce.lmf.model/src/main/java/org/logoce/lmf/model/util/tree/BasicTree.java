package org.logoce.lmf.model.util.tree;

import java.util.List;
import java.util.function.Function;

public interface BasicTree<T, Y extends BasicTree<T, Y>> extends DataNode<T>, StructuredTree<Y>
{
	/**
	 * Map a tree by converting data to another type.
	 * <p>
	 * This function map all the subtree, and is fully immutable.
	 * <p>
	 * Only convert children, doesn't convert parents, so you generally want to call this
	 * method from the root of your tree.
	 *
	 * @param mapper Convert a source Node to a target data type
	 * @param <Target> Target data type
	 * @return The converted tree
	 */
	default <Target> Tree<Target> mapTree(final Function<Y, Target> mapper)
	{
		return map(mapper, Tree::new);
	}

	/**
	 * Lazy map, map a tree by converting data to another type.
	 * <p>
	 * This function can take any node, and provide a converted node that allow exploring parents.
	 * <p>
	 * Slower and more expansive than a normal map ({@link BasicTree#mapTree}), but more efficient if you only need to convert a small
	 * part of a tree.
	 *
	 * @param mapper Convert a source Node to a target data type
	 * @param <Target> Target data type
	 * @return The converted tree
	 */
	@SuppressWarnings("unchecked")
	default <Target> LazyMappedDataTree<Target> mapLazy(final Function<T, Target> mapper)
	{
		return new LazyMappedDataTree<>((Y) this, mapper);
	}

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
																							builder))
																			.toList());
		return builder.apply(buildInfo);
	}

	record BuildInfo<T, Y>(Y parent, T data, Function<Y, List<Y>> childrenBuilder) {}
}
