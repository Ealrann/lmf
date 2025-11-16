package org.logoce.lmf.model.resource.transform.multi.internal;

import java.util.LinkedList;
import java.util.List;

public final class GroupBuilder
{
	public static List<LoadGroup> prepare(final List<BuildingModel> models)
	{
		final var builder = new GroupBuilder();
		builder.sortGroups(models);
		return List.copyOf(builder.loadGroups);
	}

	private GroupBuilder()
	{
	}

	private final LinkedList<LoadGroup> loadGroups = new LinkedList<>();

	private void sortGroups(final List<BuildingModel> loadModels)
	{
		final LinkedList<BuildingModel> remaining = new LinkedList<>(loadModels);
		while (!remaining.isEmpty())
		{
			boolean didSomething = false;
			final var it = remaining.iterator();
			while (it.hasNext())
			{
				final var elem = it.next();
				if (tryInsert(elem))
				{
					it.remove();
					didSomething = true;
				}
			}

			if (didSomething == false)
			{
				throw new IllegalStateException("Cannot resolve all imports");
			}
		}
	}

	private boolean tryInsert(final BuildingModel elem)
	{
		final var imports = elem.model().imports();
		final var matchGroup = loadGroups.stream().filter(g -> g.compare(imports)).findFirst();

		if (matchGroup.isPresent())
		{
			matchGroup.get().addTree(elem);
			return true;
		}
		else
		{
			final LinkedList<String> remainingImports = new LinkedList<>(imports);
			final var iterator = loadGroups.iterator();
			int i = 0;
			while (iterator.hasNext() && !remainingImports.isEmpty())
			{
				final var loadGroup = iterator.next();
				remainingImports.removeAll(loadGroup.collectNames());
				i++;
			}

			if (remainingImports.isEmpty())
			{
				final var newLoadGroup = new LoadGroup(imports);
				newLoadGroup.addTree(elem);
				loadGroups.add(i, newLoadGroup);
				return true;
			}
			else
			{
				return false;
			}
		}
	}
}
