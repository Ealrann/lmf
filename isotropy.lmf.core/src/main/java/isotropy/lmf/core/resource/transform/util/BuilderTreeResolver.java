package isotropy.lmf.core.resource.transform.util;

import isotropy.lmf.core.resource.transform.feature.IFeatureResolution;
import isotropy.lmf.core.resource.transform.feature.TreeToFeatureResolver;
import isotropy.lmf.core.resource.util.Tree;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class BuilderTreeResolver
{
	private final TreeToFeatureResolver treeToFeatureResolver;

	public static void resolve(final Tree<BuilderNode<?>> tree)
	{
		final var builderNode = tree.data();
		final var features = builderNode.group.features();

		//todo bake one per group and put in a map before starts
		final var featureResolver = new TreeToFeatureResolver(tree, features);
		final var resolver = new BuilderTreeResolver(featureResolver);

		final var words = builderNode.words;

		final var wordResolution = resolver.resolveWords(words);
		final var childrenResolution = resolver.resolveChildren(tree);

		final var featureResolutions = Stream.concat(wordResolution, childrenResolution)
											 .toList();

		builderNode.setFeatureResolutions(featureResolutions);
	}

	private BuilderTreeResolver(final TreeToFeatureResolver treeToFeatureResolver)
	{
		this.treeToFeatureResolver = treeToFeatureResolver;
	}

	private Stream<? extends IFeatureResolution> resolveWords(final List<String> words)
	{
		return words.stream()
					.map(treeToFeatureResolver::resolve)
					.filter(Optional::isPresent)
					.map(Optional::get);
	}

	private Stream<? extends IFeatureResolution> resolveChildren(final Tree<BuilderNode<?>> node)
	{
		return treeToFeatureResolver.resolve(node);
	}
}
