package isotropy.lmf.core.resource.transform.feature;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.resource.transform.feature.resolver.*;
import isotropy.lmf.core.resource.transform.node.BuilderNode;
import isotropy.lmf.core.resource.util.Tree;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TreeToFeatureResolver
{
	private final List<IWordResolver<?>> wordResolvers;
	private final List<IChildResolver<?>> childResolvers;

	public TreeToFeatureResolver(final List<? extends Feature<?, ?>> features)
	{
		final var resolvers = features.stream()
									  .map(TreeToFeatureResolver::buildResolver)
									  .toList();

		wordResolvers = resolvers.stream()
								 .filter(IWordResolver.class::isInstance)
								 .map(r -> (IWordResolver<?>) r)
								 .collect(Collectors.toUnmodifiableList());

		childResolvers = resolvers.stream()
								  .filter(IChildResolver.class::isInstance)
								  .map(r -> (IChildResolver<?>) r)
								  .collect(Collectors.<IChildResolver<?>>toUnmodifiableList());
	}

	public Optional<? extends IFeatureResolution> resolve(final Tree<BuilderNode<?>> tree, final String word)
	{
		final var indexEqual = word.indexOf('=');

		if (indexEqual > -1)
		{
			final var name = word.substring(0, indexEqual);
			final var value = word.substring(indexEqual + 1);
			return resolveWithNameValue(tree, name, value);
		}
		else
		{
			final var nameOnlyResolution = resolveWithName(tree, word);
			if (nameOnlyResolution.isPresent())
			{
				return nameOnlyResolution;
			}
			else
			{
				return resolveWithValue(tree, word);
			}
		}
	}

	public Stream<? extends IFeatureResolution> resolve(final Tree<BuilderNode<?>> node)
	{
		return childResolvers.stream()
							 .map(r -> r.resolve(node))
							 .filter(Optional::isPresent)
							 .map(Optional::get);
	}

	private Optional<? extends IFeatureResolution> resolveWithNameValue(final Tree<BuilderNode<?>> tree,
																		final String name,	final String value)
	{
		final var resolvedStream = wordResolvers.stream()
												.filter(r -> r.match(name))
												.map(r -> r.resolve(tree, value));
		return findAny(resolvedStream);
	}

	private Optional<? extends IFeatureResolution> resolveWithValue(final Tree<BuilderNode<?>> tree,final String word)
	{
		final var resolvedStream = wordResolvers.stream()
												.map(r -> r.resolve(tree, word));
		return findAny(resolvedStream);
	}

	private Optional<? extends IFeatureResolution> resolveWithName(final Tree<BuilderNode<?>> tree,final String word)
	{
		final var nameMatchBooleanResolvers = wordResolvers.stream()
														   .filter(IWordResolver::isBooleanAttribute)
														   .filter(r -> r.match(word))
														   .map(r -> r.resolve(tree, "true"));
		final var booleanResolution = findAny(nameMatchBooleanResolvers);
		return booleanResolution;
	}

	private static Optional<? extends IFeatureResolution> findAny(final Stream<? extends Optional<? extends IFeatureResolution>> t)
	{
		return t.filter(Optional::isPresent)
				.map(Optional::get)
				.findAny();
	}

	private static IFeatureResolver<?> buildResolver(Feature<?, ?> feature)
	{
		if (feature instanceof Attribute)
		{
			return new AttributeResolver<>((Attribute<?, ?>) feature);
		}
		else
		{
			return buildRelationResolver((Relation<?, ?>) feature);
		}
	}

	private static IFeatureResolver<?> buildRelationResolver(Relation<?, ?> relation)
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
