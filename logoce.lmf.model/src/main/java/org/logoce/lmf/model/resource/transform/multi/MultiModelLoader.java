package org.logoce.lmf.model.resource.transform.multi;

import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.transform.PModelLinker;
import org.logoce.lmf.model.resource.transform.multi.internal.BuildingModel;
import org.logoce.lmf.model.resource.transform.multi.internal.GroupBuilder;
import org.logoce.lmf.model.resource.transform.multi.internal.LoadGroup;
import org.logoce.lmf.model.resource.transform.multi.internal.LoadModel;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.List;

public final class MultiModelLoader
{
	private final List<LoadGroup> sortedGroups;
	private final List<BuildingModel> buildingModels;

	private final ModelRegistry.Builder registry;

	private boolean built = false;

	public MultiModelLoader(final List<Tree<PNode>> pTress, final ModelRegistry initialRegistry)
	{
		buildingModels = pTress.stream().map(LoadModel::from).map(BuildingModel::new).toList();
		sortedGroups = GroupBuilder.prepare(buildingModels);
		registry = new ModelRegistry.Builder(initialRegistry);
	}

	public List<Model> build()
	{
		if (built == false)
		{
			for (final var loadGroup : sortedGroups)
			{
				final var linker = new PModelLinker<PNode>(registry.build());
				loadGroup.build(linker, registry);
			}
			built = true;
		}

		return buildingModels.stream().map(BuildingModel::builtModel).toList();
	}
}
