package isotropy.lmf.core.resource.transform.word.resolver;

import isotropy.lmf.core.resource.transform.node.TreeBuilderNode;
import isotropy.lmf.core.resource.transform.word.IFeatureResolution;

import java.util.*;
import java.util.function.Function;

public class WordResolver
{
	private final List<IWordResolver<?>> availableResolvers;

	public WordResolver(final List<? extends IWordResolver<?>> wordResolvers)
	{
		this.availableResolvers = List.copyOf(wordResolvers);
	}

	public List<IFeatureResolution> resolve(final TreeBuilderNode<?> node)
	{
		final var runner = new WordResolverRunner(availableResolvers, node);
		final var wordResolutions = resolveWords(node.words());
		final List<IFeatureResolution> resolutions = new ArrayList<>();

		resolutions.addAll(runner.filterNameValueResolvers(wordResolutions.nameValueWords));
		resolutions.addAll(runner.filterSimpleResolvers(wordResolutions.simpleWords));

		return resolutions;
	}

	private static WordResolverRunner.WordsResolution resolveWords(List<String> words)
	{
		final List<String> simpleWords = new ArrayList<>();
		final List<WordResolverRunner.NameValueWord> nameValueWords = new ArrayList<>();
		for (final var word : words)
		{
			toNameValue(word).ifPresentOrElse(nameValueWords::add, () -> simpleWords.add(word));
		}
		return new WordResolverRunner.WordsResolution(simpleWords, nameValueWords);
	}

	private static Optional<WordResolverRunner.NameValueWord> toNameValue(final String word)
	{
		final var indexEqual = word.indexOf('=');
		if (indexEqual > -1)
		{
			final var name = word.substring(0, indexEqual);
			final var value = word.substring(indexEqual + 1);
			return Optional.of(new WordResolverRunner.NameValueWord(name, value));
		}
		else
		{
			return Optional.empty();
		}
	}

	private static class WordResolverRunner
	{
		private final LinkedList<IWordResolver<?>> availableResolvers;
		private final TreeBuilderNode<?> node;

		WordResolverRunner(final List<IWordResolver<?>> wordResolvers, final TreeBuilderNode<?> node)
		{

			this.availableResolvers = new LinkedList<>(wordResolvers);
			this.node = node;
		}

		public List<IFeatureResolution> filterNameValueResolvers(final List<NameValueWord> nameValueWords)
		{
			final List<IFeatureResolution> resolutions = new ArrayList<>();
			for (final var nameValueWord : nameValueWords)
			{
				final var resolved = findMandatory(r -> r.match(nameValueWord.name)
														? Optional.of(r.resolveOrThrow(node,
																					   nameValueWord.value))
														: Optional.empty(), nameValueWord.toString());
				resolutions.add(resolved);
			}

			return resolutions;
		}

		public List<IFeatureResolution> filterSimpleResolvers(final List<String> simpleWords)
		{
			final List<IFeatureResolution> resolutions = new ArrayList<>();
			final List<String> notFound = new ArrayList<>();
			for (final var word : simpleWords)
			{
				final var resolved = findOptional(r -> r.match(word)
													   ? Optional.of(r.resolveOrThrow(node, "true"))
													   : Optional.empty());
				resolved.ifPresentOrElse(resolutions::add, () -> notFound.add(word));
			}
			for (final var word : notFound)
			{
				resolutions.add(findMandatory(r -> r.resolve(node, word), word));
			}

			return resolutions;
		}

		private IFeatureResolution findMandatory(final Function<IWordResolver<?>, Optional<? extends IFeatureResolution>> resolver,
												 String message)
		{
			return findOptional(resolver).orElseThrow(() -> new NoSuchElementException(message));
		}

		private Optional<IFeatureResolution> findOptional(final Function<IWordResolver<?>, Optional<? extends IFeatureResolution>> resolver)
		{
			final var it = availableResolvers.iterator();
			while (it.hasNext())
			{
				final var currentResolver = it.next();
				final var resolution = resolver.apply(currentResolver);
				if (resolution.isPresent())
				{
					it.remove();
					return Optional.of(resolution.get());
				}
			}
			return Optional.empty();
		}

		record NameValueWord(String name, String value) {}

		record WordsResolution(List<String> simpleWords, List<NameValueWord> nameValueWords) {}
	}
}
