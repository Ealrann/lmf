package org.logoce.lmf.model.resource.transform.multi;

import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.transform.PModelLinker;
import org.logoce.lmf.model.resource.transform.multi.internal.BuildingModel;
import org.logoce.lmf.model.resource.transform.multi.internal.GroupBuilder;
import org.logoce.lmf.model.resource.transform.multi.internal.LoadGroup;
import org.logoce.lmf.model.resource.transform.multi.internal.LoadModel;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.List;

public final class MultiModelLoader
{
	private final List<LoadGroup> sortedGroups;
	private final List<BuildingModel> buildingModels;

	private boolean built = false;

	public MultiModelLoader(final List<Tree<PNode>> pTress)
	{
		buildingModels = pTress.stream().map(LoadModel::from).map(BuildingModel::new).toList();
		sortedGroups = GroupBuilder.from(buildingModels);
	}

	public List<Model> build(final PModelLinker<PNode> pmodelLinker)
	{
		if (built == false)
		{
			for (final var loadGroup : sortedGroups)
			{
				loadGroup.build(pmodelLinker);
			}
			built = true;
		}

		return buildingModels.stream().map(BuildingModel::builtModel).toList();
	}
}
