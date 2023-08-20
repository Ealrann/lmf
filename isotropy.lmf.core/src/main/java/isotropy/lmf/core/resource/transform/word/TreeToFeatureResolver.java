package isotropy.lmf.core.resource.transform.word;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.resource.transform.node.TreeBuilderNode;
import isotropy.lmf.core.resource.transform.word.resolver.IWordResolver;
import isotropy.lmf.core.resource.transform.word.resolver.WordResolver;

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
								.map(IWordResolver::buildResolver)
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
		final var builderNode = node.data();
		final var words = builderNode.words();
		final var wordResolver = new WordResolver(wordResolvers);
		final var wordResolution = wordResolver.resolve(node, words);

		node.setResolutions(wordResolution);
	}

	public Stream<? extends Relation<?, ?>> streamContainmentRelations()
	{
		return containmentRelations.stream();
	}

	public Group<?> group()
	{
		return group;
	}
}
