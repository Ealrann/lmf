package isotropy.lmf.core.resource.transform.feature.resolver;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.resource.transform.feature.IFeatureResolution;
import isotropy.lmf.core.resource.transform.util.BuilderNode;
import isotropy.lmf.core.resource.util.Tree;

import java.util.List;
import java.util.Optional;

public final class ContainmentResolver<T extends LMObject> extends AbstractResolver<T, Relation<T, ?>> implements
																									   IChildResolver<T>
{
	public ContainmentResolver(final Relation<T, ?> relation)
	{
		super(relation);
		assert relation.contains();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<IFeatureResolution> resolve(Tree<BuilderNode<?>> node)
	{
		final var preciseChildren = node.children()
										.stream()
										.map(Tree::data)
										.filter(n -> feature.name()
															.equals(n.name))
										.map(n -> (BuilderNode<T>) n)
										.toList();

		if (!preciseChildren.isEmpty())
		{
			return Optional.of(new ChildrenResolution<>(feature, preciseChildren));
		}
		else
		{
			final var group = feature.group();
			final var guessedChildren = node.children()
											.stream()
											.map(Tree::data)
											.filter(n -> group == n.group)
											.map(n -> (BuilderNode<T>) n)
											.toList();

			return Optional.of(new ChildrenResolution<>(feature, guessedChildren));
		}
	}

	public static final class ChildrenResolution<T extends LMObject> implements IFeatureResolution
	{
		final Relation<T, ?> relation;
		final List<BuilderNode<T>> builders;

		private ChildrenResolution(final Relation<T, ?> relation, final List<BuilderNode<T>> builders)
		{
			this.relation = relation;
			this.builders = builders;
		}

		@Override
		public void pushValue(final IFeaturedObject.Builder<?> builder)
		{
			for (final var b : builders)
			{
				builder.push(relation, b::build);
			}
		}
	}
}
