package isotropy.lmf.core.resource.transform;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.model.IModelPackage;
import isotropy.lmf.core.model.ModelRegistry;
import isotropy.lmf.core.resource.transform.util.BuilderNode;
import isotropy.lmf.core.resource.transform.util.BuilderTreeResolver;
import isotropy.lmf.core.resource.util.Tree;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PTreeToJava
{
	private final Map<String, Alias> aliases;
	private final Map<String, ModelGroup> groups;

	public PTreeToJava()
	{
		aliases = ModelRegistry.Instance.models()
										.map(IModelPackage::model)
										.map(Model::aliases)
										.flatMap(Collection::stream)
										.collect(Collectors.toUnmodifiableMap(Named::name, Function.identity()));
		groups = ModelRegistry.Instance.models()
									   .flatMap(PTreeToJava::modelGroups)
									   .collect(Collectors.toUnmodifiableMap(ModelGroup::name, Function.identity()));
	}

	public List<LMObject> transform(final Tree<List<String>> tree)
	{
		final var builderTrees = tree.children()
									 .stream()
									 .map(t -> t.map(this::convert))
									 .toList();

		builderTrees.stream()
					.flatMap(Tree::stream)
					.forEach(BuilderTreeResolver::resolve);

		return builderTrees.stream()
						   .map(Tree::data)
						   .map(bd -> bd.builder)
						   .map(IFeaturedObject.Builder::build)
						   .map(LMObject.class::cast)
						   .toList();
	}

	private BuilderNode convert(final List<String> words)
	{
		final var namedNode = extractWords(words);
		final var modelGroup = groups.get(namedNode.name);
		final var group = modelGroup.group;
		final var builder = modelGroup.builder();
		return new BuilderNode(namedNode.words, builder, group);
	}

	private NamedNode extractWords(final List<String> words)
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

	private static Stream<ModelGroup> modelGroups(final IModelPackage model)
	{
		return model.model()
					.groups()
					.stream()
					.map(group -> new ModelGroup(model, group));
	}

	private record NamedNode(String name, List<String> words)
	{}

	private record ModelGroup(IModelPackage modelPackage, Group<?> group)
	{
		public String name()
		{
			return group.name();
		}

		public IFeaturedObject.Builder<?> builder()
		{
			return modelPackage.builder(group);
		}
	}
}
