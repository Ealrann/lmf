package org.logoce.lmf.editor.highlight.proposition;

import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.util.ModelRegistry;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GroupComputer
{
	private final List<Relation<?, ?>> relations;
	private final boolean fullGroup;
	private final List<Concept<?>> concepts;

	public GroupComputer(final List<Relation<?, ?>> relations, final boolean fullGroup)
	{
		this.relations = relations;
		this.concepts = relations.stream()
								 .map(Relation::reference)
								 .map(Reference::group)
								 .map(g -> (Group<?>) g)
								 .collect(Collectors.toUnmodifiableList());
		this.fullGroup = fullGroup;
	}

	public Stream<String> computeEntries()
	{
		return relations.stream().flatMap(this::streamProposals);
	}

	@NotNull
	private Stream<String> streamProposals(final Relation<?, ?> relation)
	{
		final var concept = relation.reference().group();
		if (concept instanceof Group<?> g)
		{
			final var relationName = relation.name();

			if (relation.contains() == false)
			{
				return Stream.of(relationName + "=");
			}
			else
			{
				return streamContainsProposal(g, relationName).map(this::encapsulate);
			}
		}
		else
		{
			return Stream.empty();
		}
	}

	private String encapsulate(final String value)
	{
		return fullGroup ? '(' + value + ')' : value;
	}

	@NotNull
	private Stream<String> streamContainsProposal(final Group<?> group, final String relationName)
	{
		final var isUnique = 1 == concepts.stream().filter(group::equals).count();

		final var groupName = group.name();
		if (isUnique)
		{
			if (group.concrete())
			{
				final var value = nameMatches(relationName, groupName) ? groupName : relationName;
				return Stream.of(value).mapMulti(GroupComputer::collectAliases);
			}
			else
			{
				return ModelRegistry.Instance.streamChildGroups(group)
											 .map(Named::name)
											 .mapMulti(GroupComputer::collectAliases);
			}
		}
		else
		{
			return ModelRegistry.Instance.streamChildGroups(group)
										 .map(childGroup -> relationName + "=" + childGroup.name());
		}
	}

	private static void collectAliases(final String value, Consumer<String> collector)
	{
		collector.accept(value);
		ModelRegistry.Instance.models()
							  .flatMap(m -> m.model().aliases().stream())
							  .filter(alias -> alias.value().startsWith(value))
							  .map(Named::name)
							  .forEach(collector);
	}

	private static boolean nameMatches(final String relationName, final String groupName)
	{
		return relationName.compareToIgnoreCase(groupName) == 0 ||
			   relationName.compareToIgnoreCase(groupName + "s") == 0;
	}
}
