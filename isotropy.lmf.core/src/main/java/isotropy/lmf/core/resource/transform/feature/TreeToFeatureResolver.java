package isotropy.lmf.core.resource.transform.feature;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.resource.transform.feature.resolver.*;
import isotropy.lmf.core.resource.transform.util.BuilderNode;
import isotropy.lmf.core.resource.util.Tree;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TreeToFeatureResolver
{
	private final Tree<BuilderNode> tree;
	private final List<IFeatureResolver<?>> resolvers;

	public TreeToFeatureResolver(final Tree<BuilderNode> tree, final List<? extends Feature<?, ?>> features)
	{
		this.tree = tree;

		resolvers = features.stream()
							.map(TreeToFeatureResolver::buildResolver)
							.collect(Collectors.toUnmodifiableList());
	}

	public Optional<? extends IFeatureResolution> resolve(final String word)
	{
		return wordResolvers().map(r -> r.resolve(tree, word))
							  .filter(Optional::isPresent)
							  .map(Optional::get)
							  .findAny();
	}

	public Optional<? extends IFeatureResolution> resolve(final BuilderNode child)
	{
		return childResolvers().map(r -> r.resolve(child))
							   .filter(Optional::isPresent)
							   .map(Optional::get)
							   .findAny();
	}

	@SuppressWarnings("unchecked")
	private Stream<IWordResolver<?>> wordResolvers()
	{
		return resolvers.stream()
						.filter(IWordResolver.class::isInstance)
						.map(IWordResolver.class::cast);
	}

	@SuppressWarnings("unchecked")
	private Stream<IChildResolver<?>> childResolvers()
	{
		return resolvers.stream()
						.filter(IChildResolver.class::isInstance)
						.map(IChildResolver.class::cast);
	}

	@SuppressWarnings("unchecked")
	private static <T> IFeatureResolver<T> buildResolver(Feature<T, ?> feature)
	{
		if (feature instanceof Attribute)
		{
			return new AttributeResolver<>((Attribute<T, ?>) feature);
		}
		else
		{
			return (IFeatureResolver<T>) buildRelationResolver((Relation<?, ?>) feature);
		}
	}

	private static <T extends LMObject> IFeatureResolver<T> buildRelationResolver(Relation<T, ?> relation)
	{
		if (relation.contains())
		{
			return new ContainmentResolver<>(relation);
		}
		else
		{
			return new ReferenceResolver<>(relation);
		}
	}
}
