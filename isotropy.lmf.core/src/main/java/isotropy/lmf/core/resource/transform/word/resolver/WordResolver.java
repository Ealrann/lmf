package isotropy.lmf.core.resource.transform.word.resolver;

import isotropy.lmf.core.resource.transform.node.TreeBuilderNode;
import isotropy.lmf.core.resource.transform.word.IFeatureResolution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class WordResolver
{
	private final List<IWordResolver<?>> availableResolvers;

	public WordResolver(final List<IWordResolver<?>> wordResolvers)
	{
		this.availableResolvers = List.copyOf(wordResolvers);
	}

	public List<IFeatureResolution> resolve(final TreeBuilderNode<?> node, final List<String> words)
	{
		final var runner = new WordResolverRunner(availableResolvers, node, words);
		return runner.resolve();
	}

	private static class WordResolverRunner
	{
		private final LinkedList<IWordResolver<?>> availableResolvers;
		private final TreeBuilderNode<?> node;
		private final WordsResolution wordResolutions;

		WordResolverRunner(final List<IWordResolver<?>> wordResolvers,
						   final TreeBuilderNode<?> node,
						   final List<String> words)
		{

			this.availableResolvers = new LinkedList<>(wordResolvers);
			this.wordResolutions = resolveWords(words);
			this.node = node;
		}

		public List<IFeatureResolution> resolve()
		{
			final List<IFeatureResolution> resolutions = new ArrayList<>();

			resolutions.addAll(filterNameValueResolvers(wordResolutions.nameValueWords, node));
			resolutions.addAll(filterSimpleResolvers(wordResolutions.simpleWords, node));

			return resolutions;
		}

		private List<IFeatureResolution> filterNameValueResolvers(final List<NameValueWord> nameValueWords,
																  final TreeBuilderNode<?> node)
		{
			final List<IFeatureResolution> resolutions = new ArrayList<>();
			for (final var nameValueWord : nameValueWords)
			{
				final var it = availableResolvers.iterator();
				while (it.hasNext())
				{
					final var currentResolver = it.next();
					if (currentResolver.match(nameValueWord.name))
					{
						final var resolution = currentResolver.resolve(node, nameValueWord.value)
															  .orElseThrow();
						resolutions.add(resolution);
						it.remove();
					}
				}
			}

			return resolutions;
		}

		private List<IFeatureResolution> filterSimpleResolvers(final List<String> simpleWords,
															   final TreeBuilderNode<?> node)
		{
			final List<IFeatureResolution> resolutions = new ArrayList<>();
			final List<String> notFound = new ArrayList<>();

			for (final var word : simpleWords)
			{
				boolean found = false;
				final var it = availableResolvers.iterator();
				while (it.hasNext())
				{
					final var currentResolver = it.next();
					if (currentResolver.match(word))
					{
						final var resolution = currentResolver.resolve(node, "true")
															  .orElseThrow();
						resolutions.add(resolution);
						it.remove();
						found = true;
					}
				}
				if (!found)
				{
					notFound.add(word);
				}
			}
			for (final var word : notFound)
			{
				boolean found = false;
				final var it = availableResolvers.iterator();
				while (it.hasNext())
				{
					final var currentResolver = it.next();
					final var resolution = currentResolver.resolve(node, word);
					if (resolution.isPresent())
					{
						resolutions.add(resolution.get());
						it.remove();
						found = true;
					}
				}
				if (!found)
				{
					notFound.add(word);
				}
			}

			return resolutions;
		}

		private static Optional<NameValueWord> toNameValue(final String word)
		{
			final var indexEqual = word.indexOf('=');
			if (indexEqual > -1)
			{
				final var name = word.substring(0, indexEqual);
				final var value = word.substring(indexEqual + 1);
				return Optional.of(new NameValueWord(name, value));
			}
			else
			{
				return Optional.empty();
			}
		}

		private static WordsResolution resolveWords(List<String> words)
		{
			final List<String> simpleWords = new ArrayList<>();
			final List<NameValueWord> nameValueWords = new ArrayList<>();
			for (final var word : words)
			{
				toNameValue(word).ifPresentOrElse(nameValueWords::add, () -> simpleWords.add(word));
			}
			return new WordsResolution(simpleWords, nameValueWords);
		}

		record NameValueWord(String name, String value) {}

		record WordsResolution(List<String> simpleWords, List<NameValueWord> nameValueWords) {}
	}

}
