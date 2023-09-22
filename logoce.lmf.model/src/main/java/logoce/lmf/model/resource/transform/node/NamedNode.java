package logoce.lmf.model.resource.transform.node;

import logoce.lmf.model.lang.Alias;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record NamedNode(String name, List<String> words)
{
	public static final class Builder
	{
		private final Map<String, Alias> aliases;

		public Builder(Map<String, Alias> aliases)
		{
			this.aliases = aliases;
		}

		public NamedNode build(final List<String> words)
		{

			if (words.get(0)
					 .equals(Alias.class.getSimpleName()))
			{
				return new NamedNode(words.get(0),
									 words.stream()
										  .skip(1)
										  .toList());
			}
			else
			{
				final var it = words.stream()
									.flatMap(this::alias)
									.iterator();
				final var name = it.next();
				final var subWords = new ArrayList<String>();
				it.forEachRemaining(subWords::add);

				return new NamedNode(name, Collections.unmodifiableList(subWords));
			}
		}

		private Stream<String> alias(final String word)
		{
			if (aliases.containsKey(word))
			{
				return aliases.get(word)
							  .words()
							  .stream();
			}
			else
			{
				return Stream.of(word);
			}
		}
	}
}