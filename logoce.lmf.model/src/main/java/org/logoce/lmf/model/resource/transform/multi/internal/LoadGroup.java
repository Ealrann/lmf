package org.logoce.lmf.model.resource.transform.multi.internal;

import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.transform.PModelLinker;
import org.logoce.lmf.model.util.ModelRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LoadGroup
{
	final Set<String> imports;

	final List<BuildingModel> models = new ArrayList<>();

	public LoadGroup(List<String> imports)
	{
		this.imports = new HashSet<>(imports);
	}

	public void addTree(BuildingModel model)
	{
		models.add(model);
	}

	public List<String> collectNames()
	{
		return models.stream().map(BuildingModel::LoadModel).map(LoadModel::qualifiedName).toList();
	}

	public boolean compare(List<String> otherImports)
	{
		return imports.size() == otherImports.size() && imports.containsAll(otherImports);
	}

	public void build(final PModelLinker<PNode> pmodelLinker)
	{
		final var trees = models.stream().map(BuildingModel::LoadModel).map(LoadModel::tree).toList();
		final var builtTrees = pmodelLinker.build(trees);

		for (int i = 0; i < builtTrees.size(); i++)
		{
			final var built = builtTrees.get(i);
			final var building = models.get(i);

			if (built instanceof Model model)
			{
				building.setBuiltModel(model);
				ModelRegistry.Instance.register(model);
			}
			else
			{
				throw new IllegalArgumentException("This input doesn't define a valid model. Use loadObject()" +
												   " instead.");
			}
		}
	}
}
