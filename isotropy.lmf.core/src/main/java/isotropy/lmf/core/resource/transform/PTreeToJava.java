package isotropy.lmf.core.resource.transform;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.lang.Named;
import isotropy.lmf.core.model.IModelPackage;
import isotropy.lmf.core.model.ModelRegistry;
import isotropy.lmf.core.resource.transform.node.ModelGroup;
import isotropy.lmf.core.resource.transform.node.NamedNode;
import isotropy.lmf.core.resource.transform.node.TreeBuilderNode;
import isotropy.lmf.core.resource.transform.node.TreeBuilderNodeBuilder;
import isotropy.lmf.core.resource.transform.word.TreeToFeatureResolver;
import isotropy.lmf.core.resource.util.Tree;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PTreeToJava
{
	private final Map<Group<?>, TreeToFeatureResolver> resolvers;

	private final TreeBuilderNodeBuilder nodeMapper;

	public PTreeToJava()
	{
		final var aliases = ModelRegistry.Instance.models()
												  .map(IModelPackage::model)
												  .map(Model::aliases)
												  .flatMap(Collection::stream)
												  .collect(Collectors.toUnmodifiableMap(Named::name,
																						Function.identity()));

		final var namedNodeBuilder = new NamedNode.Builder(aliases);

		final var groups = ModelRegistry.Instance.models()
												 .flatMap(PTreeToJava::modelGroups)
												 .collect(Collectors.toUnmodifiableMap(ModelGroup::name,
																					   Function.identity()));

		resolvers = groups.values()
						  .stream()
						  .map(ModelGroup::group)
						  .map(TreeToFeatureResolver::new)
						  .collect(Collectors.toUnmodifiableMap(TreeToFeatureResolver::group, Function.identity()));

		nodeMapper = new TreeBuilderNodeBuilder(namedNodeBuilder, groups, resolvers);
	}

	public List<? extends LMObject> transform(final Tree<List<String>> tree)
	{
		final var builderTrees = tree.children()
									 .stream()
									 .map(nodeMapper::mapTree)
									 .toList();

		builderTrees.stream()
					.flatMap(TreeBuilderNode::stream)
					.forEach(this::resolve);

		return builderTrees.stream()
						   .map(TreeBuilderNode::build)
						   .toList();
	}

	private void resolve(TreeBuilderNode<?> node)
	{
		final var resolver = resolvers.get(node.data()
											   .modelGroup()
											   .group());
		resolver.resolve(node);
	}

	private static Stream<ModelGroup<?>> modelGroups(final IModelPackage model)
	{
		return model.model()
					.groups()
					.stream()
					.map(group -> new ModelGroup<>(model, group));
	}

}
