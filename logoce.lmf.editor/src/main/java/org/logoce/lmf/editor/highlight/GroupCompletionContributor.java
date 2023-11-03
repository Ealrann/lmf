package org.logoce.lmf.editor.highlight;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.parser.PNodeView;
import org.logoce.lmf.editor.psi.LMFGroup;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.resource.transform.PModelBuilder;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.tree.TreeView;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GroupCompletionContributor extends CompletionContributor
{
	private final static PModelBuilder<PNodeView> PMDEL_BUILDER = new PModelBuilder<>();

	public GroupCompletionContributor()
	{
		extend(CompletionType.BASIC,
			   PlatformPatterns.or(PlatformPatterns.psiElement(LMIntellijTokenTypes.TYPE),
								   PlatformPatterns.psiElement(LMIntellijTokenTypes.TYPE_NAME)),
			   new GroupCompletionProvider((elem) -> (LMFGroup) elem.getParent().getParent(), false));
	}

	private static final class GroupCompletionProvider extends CompletionProvider<CompletionParameters>
	{
		private final Function<PsiElement, LMFGroup> groupRetriever;
		private final boolean fullGroup;

		public GroupCompletionProvider(final Function<PsiElement, LMFGroup> groupSupplier, final boolean fullGroup)
		{
			this.groupRetriever = groupSupplier;
			this.fullGroup = fullGroup;
		}

		@Override
		public void addCompletions(@NotNull CompletionParameters parameters,
								   @NotNull ProcessingContext context,
								   @NotNull CompletionResultSet resultSet)
		{
			final var elem = parameters.getPosition();
			final var group = groupRetriever.apply(elem);
			computeFromGroup(resultSet, group);
		}

		private void computeFromGroup(final @NotNull CompletionResultSet resultSet, final LMFGroup lmGroup)
		{
			final var astGroup = lmGroup.getParent().getNode();
			final var groupNode = PNodeView.of(astGroup);
			final var treeView = new TreeView<>(groupNode, PNodeView::children, PNodeView::parent);

			final var link = PMDEL_BUILDER.linkPartialUnresolved(treeView, (a, b) -> {b.printStackTrace();});

			if (link != null)
			{
				final var group = link.group();
				final var computer = PropositionComputer.of(group);
				final var entries = computer.computeEntries().map(this::bake).toList();

				resultSet.addAllElements(entries);
			}
		}

		private LookupElementBuilder bake(final String value)
		{
			if (fullGroup)
			{
				return LookupElementBuilder.create("(" + value + ")");
			}
			else
			{
				return LookupElementBuilder.create(value);
			}
		}
	}

	private record PropositionComputer(List<Relation<?, ?>> relations, List<Concept<?>> concepts)
	{
		public static PropositionComputer of(Group<?> parentGroup)
		{
			final List<Relation<?, ?>> relations = parentGroup.features()
															  .stream()
															  .filter(Relation.class::isInstance)
															  .map(r -> (Relation<?, ?>) r)
															  .collect(Collectors.toUnmodifiableList());

			final List<Concept<?>> concepts = relations.stream()
													   .map(Relation::reference)
													   .map(Reference::group)
													   .map(g -> (Group<?>) g)
													   .collect(Collectors.toUnmodifiableList());

			return new PropositionComputer(relations, concepts);
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
				if (g.concrete())
				{
					final var isUnique = 1 == concepts.stream().filter(g::equals).count();

					final var groupName = g.name();
					if (isUnique)
					{
						final var aliases = ModelRegistry.Instance.models()
																  .flatMap(m -> m.model().aliases().stream())
																  .filter(alias -> alias.value().startsWith(groupName))
																  .map(Named::name);

						return Stream.concat(Stream.of(groupName), aliases);
					}
					else
					{
						return Stream.of(relationName + "=" + groupName);
					}
				}
				else
				{
					return ModelRegistry.Instance.streamChildGroups(g)
												 .map(childGroup -> relationName + "=" + childGroup.name());
				}
			}
			else
			{
				return Stream.empty();
			}
		}
	}
}
