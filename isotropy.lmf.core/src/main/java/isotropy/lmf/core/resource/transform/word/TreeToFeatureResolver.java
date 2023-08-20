package isotropy.lmf.core.resource.transform.word;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.resource.transform.node.TreeBuilderNode;
import isotropy.lmf.core.resource.transform.word.resolver.IWordResolver;
import isotropy.lmf.core.resource.transform.word.resolver.WordResolver;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TreeToFeatureResolver
{
	private final List<? extends Relation<?, ?>> containmentRelations;

	private final Group<?> group;
	private final WordResolver wordResolver;

	public TreeToFeatureResolver(final Group<?> group)
	{
		this.group = group;
		final var features = group.features();
		final var wordResolvers = features.stream()
										  .map(IWordResolver::buildResolver)
										  .filter(Optional::isPresent)
										  .map(Optional::get)
										  .toList();

		containmentRelations = features.stream()
									   .filter(Relation.class::isInstance)
									   .map(r -> (Relation<?, ?>) r)
									   .filter(Relation::contains)
									   .toList();

		wordResolver = new WordResolver(wordResolvers);
	}

	public void resolve(final TreeBuilderNode<?> node)
	{
		final var wordResolutions = wordResolver.resolve(node);

		node.setResolutions(wordResolutions);
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
