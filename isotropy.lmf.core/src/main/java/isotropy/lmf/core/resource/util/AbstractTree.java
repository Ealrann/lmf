package isotropy.lmf.core.resource.util;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public abstract class AbstractTree<T, Y extends AbstractTree<T, Y>>
{
	private final T data;
	protected final Y parent;
	protected final List<Y> children;

	public AbstractTree(final Y parent, final T data, final Function<Y, List<Y>> childrenBuilder)
	{
		this.parent = parent;
		this.data = data;
		this.children = childrenBuilder.apply((Y) this);
	}

	public AbstractTree(final BuildInfo<T, Y> info)
	{
		this.parent = info.parent;
		this.data = info.data;
		this.children = info.childrenBuilder.apply((Y) this);
	}

	public final Y parent()
	{
		return parent;
	}

	public final T data()
	{
		return data;
	}

	public final List<Y> children()
	{
		return children;
	}

	public final Y root()
	{
		return parent == null ? (Y) this : parent.root();
	}

	public final <Target, TargetTree extends AbstractTree<Target, TargetTree>> TargetTree map(final Function<Y, Target> mapper,
																							  final Function<BuildInfo<Target, TargetTree>, TargetTree> builder)
	{
		return map(null, mapper, builder);
	}

	protected <Target, TargetTree extends AbstractTree<Target, TargetTree>> TargetTree map(final TargetTree mappedParent,
																						   final Function<Y, Target> mapper,
																						   final Function<BuildInfo<Target, TargetTree>, TargetTree> builder)
	{

		final var buildInfo = new BuildInfo<Target, TargetTree>(mappedParent,
																mapper.apply((Y) this),
																treeParent -> children.stream()
																					  .map(c -> c.map(treeParent,
																									  mapper,
																									  builder))
																					  .toList());
		return builder.apply(buildInfo);
	}

	public final Stream<Y> stream()
	{
		final var us = Stream.of((Y) this);
		final var bellow = children.stream()
								   .flatMap(AbstractTree::stream);

		return Stream.concat(us, bellow);
	}

	public final void forEach(Consumer<Y> action)
	{
		stream().forEach(action);
	}

	public record BuildInfo<T, Y extends AbstractTree<T, Y>>(Y parent, T data, Function<Y, List<Y>> childrenBuilder) {}
}
