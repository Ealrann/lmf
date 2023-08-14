package isotropy.lmf.core.resource.util;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Tree<T>
{
	private final Tree<T> parent;
	private final T data;
	private final List<Tree<T>> children;

	public Tree(final Tree<T> parent, final T data, final Function<Tree<T>, List<Tree<T>>> childrenBuilder)
	{
		this.parent = parent;
		this.data = data;
		this.children = childrenBuilder.apply(this);
	}

	public T data()
	{
		return data;
	}

	public Tree<T> parent()
	{
		return parent;
	}

	public List<Tree<T>> children()
	{
		return children;
	}

	public <Target> Tree<Target> map(final Function<T, Target> mapper)
	{
		return map(null, mapper);
	}

	private <Target> Tree<Target> map(final Tree<Target> mappedParent, final Function<T, Target> mapper)
	{
		return new Tree<>(mappedParent,
						  mapper.apply(data),
						  treeParent -> children.stream()
												.map(c -> c.map(treeParent, mapper))
												.toList());
	}

	public Stream<Tree<T>> stream()
	{
		return Stream.concat(Stream.of(this),
							 children.stream()
									 .flatMap(Tree::stream));
	}

	public void forEach(Consumer<Tree<T>> action)
	{
		stream().forEach(action);
	}
}
