package org.logoce.lmf.model.resource.transform;

import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.resource.interpretation.LMInterpreter;
import org.logoce.lmf.model.resource.interpretation.PGroup;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.linking.ModelGroup;
import org.logoce.lmf.model.resource.linking.LinkerNode;
import org.logoce.lmf.model.resource.linking.LinkerNodeBuilder;
import org.logoce.lmf.model.resource.transform.word.TreeToFeatureResolver;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.Tree;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PModelBuilder
{
	private final Map<Group<?>, TreeToFeatureResolver> resolvers;

	private final LinkerNodeBuilder linker;
	private final LMInterpreter interpreter;

	public PModelBuilder()
	{
		final var groups = ModelRegistry.Instance.models()
												 .flatMap(PModelBuilder::modelGroups)
												 .collect(Collectors.toUnmodifiableMap(ModelGroup::name,
																					   Function.identity()));

		resolvers = groups.values()
						  .stream()
						  .map(ModelGroup::group)
						  .map(TreeToFeatureResolver::new)
						  .collect(Collectors.toUnmodifiableMap(TreeToFeatureResolver::group, Function.identity()));

		interpreter = new LMInterpreter(ModelRegistry.Instance.getAliasMap());

		linker = new LinkerNodeBuilder(groups, resolvers);
	}

	public PModel link(final List<Tree<PNode>> roots)
	{
		final var linkerTrees = roots.stream().map(this::interpretTree).map(linker::mapTree).toList();
		linkerTrees.stream().flatMap(LinkerNode::stream).forEach(this::link);
		return new PModel(linkerTrees);
	}

	public List<? extends LMObject> build(final List<Tree<PNode>> roots)
	{
		return link(roots).build();
	}

	private Tree<PGroup> interpretTree(Tree<PNode> root)
	{
		return root.map(interpreter::parseTreeNode);
	}

	private void link(LinkerNode<?> node)
	{
		final var resolver = resolvers.get(node.data().modelGroup().group());
		resolver.resolve(node);
	}

	private static Stream<ModelGroup<?>> modelGroups(final IModelPackage model)
	{
		return model.model().groups().stream().map(group -> new ModelGroup<>(model, group));
	}
}
