package isotropy.lmf.core.resource.transform.word;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.resource.transform.node.TreeBuilderNode;
import isotropy.lmf.core.resource.transform.word.resolver.AttributeResolver;
import isotropy.lmf.core.resource.transform.word.resolver.IWordResolver;
import isotropy.lmf.core.resource.transform.word.resolver.ReferenceResolver;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TreeToFeatureResolver
{
	private final List<IWordResolver<?>> wordResolvers;
	private final List<? extends Relation<?, ?>> containmentRelations;

	private final Group<?> group;

	public TreeToFeatureResolver(final Group<?> group)
	{
		this.group = group;
		final var features = group.features();
		wordResolvers = features.stream()
								.map(TreeToFeatureResolver::buildResolver)
								.filter(Optional::isPresent)
								.map(Optional::get)
								.collect(Collectors.toUnmodifiableList());

		containmentRelations = features.stream()
									   .filter(Relation.class::isInstance)
									   .map(r -> (Relation<?, ?>) r)
									   .filter(Relation::contains)
									   .toList();
	}

	public void resolve(final TreeBuilderNode<?> node)
	{
		final var wordResolution = resolveWords(node);

		node.setResolutions(wordResolution);
	}

	public Stream<? extends Relation<?, ?>> streamContainmentRelations()
	{
		return containmentRelations.stream();
	}

	private Stream<? extends IFeatureResolution> resolveWords(final TreeBuilderNode<?> node)
	{
		final var builderNode = node.data();
		final var words = builderNode.words();
		return words.stream()
					.map(word -> resolve(node, word))
					.filter(Optional::isPresent)
					.map(Optional::get);
	}

	private Optional<? extends IFeatureResolution> resolve(final TreeBuilderNode<?> node, final String word)
	{
		final var indexEqual = word.indexOf('=');

		if (indexEqual > -1)
		{
			final var name = word.substring(0, indexEqual);
			final var value = word.substring(indexEqual + 1);
			return resolveWithNameValue(node, name, value);
		}
		else
		{
			final var nameOnlyResolution = resolveWithName(node, word);
			if (nameOnlyResolution.isPresent())
			{
				return nameOnlyResolution;
			}
			else
			{
				return resolveWithValue(node, word);
			}
		}
	}

	private Optional<? extends IFeatureResolution> resolveWithNameValue(final TreeBuilderNode<?> node,
																		final String name,
																		final String value)
	{
		final var resolvedStream = wordResolvers.stream()
												.filter(r -> r.match(name))
												.map(r -> r.resolve(node, value));
		return findAny(resolvedStream);
	}

	private Optional<? extends IFeatureResolution> resolveWithValue(final TreeBuilderNode<?> node, final String word)
	{
		final var resolvedStream = wordResolvers.stream()
												.map(r -> r.resolve(node, word));
		return findAny(resolvedStream);
	}

	private Optional<? extends IFeatureResolution> resolveWithName(final TreeBuilderNode<?> node, final String word)
	{
		final var nameMatchBooleanResolvers = wordResolvers.stream()
														   .filter(IWordResolver::isBooleanAttribute)
														   .filter(r -> r.match(word))
														   .map(r -> r.resolve(node, "true"));
		final var booleanResolution = findAny(nameMatchBooleanResolvers);
		return booleanResolution;
	}

	private static Optional<? extends IFeatureResolution> findAny(final Stream<? extends Optional<? extends IFeatureResolution>> t)
	{
		return t.filter(Optional::isPresent)
				.map(Optional::get)
				.findAny();
	}

	private static Optional<IWordResolver<?>> buildResolver(Feature<?, ?> feature)
	{
		if (feature instanceof Attribute)
		{
			return Optional.of(new AttributeResolver<>((Attribute<?, ?>) feature));
		}
		else
		{
			return buildRelationResolver((Relation<?, ?>) feature);
		}
	}

	private static Optional<IWordResolver<?>> buildRelationResolver(Relation<?, ?> relation)
	{
		if (!relation.contains())
		{
			return Optional.of(new ReferenceResolver<>(relation));
		}
		else
		{
			return Optional.empty();
		}
	}

	public Group<?> group()
	{
		return group;
	}
}
