package isotropy.lmf.core.resource.transform.util;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.resource.transform.feature.IFeatureResolution;
import isotropy.lmf.core.resource.transform.feature.TreeToFeatureResolver;
import isotropy.lmf.core.resource.transform.node.BuilderNode;
import isotropy.lmf.core.resource.util.Tree;

import java.util.Optional;
import java.util.stream.Stream;

public final class BuilderTreeResolver
{
	private final TreeToFeatureResolver resolver;
	private final Group<?> group;

	public BuilderTreeResolver(final Group<?> group)
	{
		this.group = group;
		final var features = group.features();
		resolver = new TreeToFeatureResolver(features);
	}

	public void resolve(final Tree<BuilderNode<?>> tree)
	{
		final var builderNode = tree.data();
		final var childrenResolution = resolveChildren(tree);
		final var wordResolution = resolveWords(tree);

		builderNode.setResolutions(wordResolution, childrenResolution);
	}

	private Stream<? extends IFeatureResolution> resolveWords(final Tree<BuilderNode<?>> node)
	{
		final var builderNode = node.data();
		final var words = builderNode.words;
		return words.stream()
					.map(word -> resolver.resolve(node, word))
					.filter(Optional::isPresent)
					.map(Optional::get);
	}

	private Stream<? extends IFeatureResolution> resolveChildren(final Tree<BuilderNode<?>> node)
	{
		return resolver.resolve(node);
	}

	public Group<?> group()
	{
		return group;
	}
}
