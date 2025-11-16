package org.logoce.lmf.editor.highlight;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.highlight.proposition.AttributeComputer;
import org.logoce.lmf.editor.highlight.proposition.GroupComputer;
import org.logoce.lmf.editor.parser.PNodeView;
import org.logoce.lmf.editor.psi.LMFGroup;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.transform.PModelLinker;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.tree.TreeView;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GroupCompletionContributor extends CompletionContributor
{
	private final static PModelLinker<PNodeView> PMDEL_BUILDER = new PModelLinker<>(ModelRegistry.empty());

	public GroupCompletionContributor()
	{
		extend(CompletionType.BASIC,
			   PlatformPatterns.or(PlatformPatterns.psiElement(LMIntellijTokenTypes.TYPE),
								   PlatformPatterns.psiElement(LMIntellijTokenTypes.TYPE_NAME)),
			   new GroupCompletionProvider(GroupCompletionContributor::exploreParentParentGroup, false, false));
		extend(CompletionType.BASIC,
			   PlatformPatterns.or(PlatformPatterns.psiElement(LMIntellijTokenTypes.VALUE),
								   PlatformPatterns.psiElement(LMIntellijTokenTypes.VALUE_NAME)),
			   new GroupCompletionProvider(GroupCompletionContributor::exploreParentGroup, true, true));
	}

	private static LMFGroup exploreParentParentGroup(final PsiElement element)
	{
		return exploreParentGroup(exploreParentGroup(element).getParent());
	}

	private static LMFGroup exploreParentGroup(final PsiElement element)
	{
		if (element instanceof LMFGroup group) return group;
		else return exploreParentGroup(element.getParent());
	}

	private static final class GroupCompletionProvider extends CompletionProvider<CompletionParameters>
	{
		private final Function<PsiElement, LMFGroup> groupRetriever;
		private final boolean fullGroup;
		private final boolean listAttributes;

		public GroupCompletionProvider(final Function<PsiElement, LMFGroup> groupSupplier,
									   final boolean fullGroup,
									   final boolean listAttributes)
		{
			this.groupRetriever = groupSupplier;
			this.fullGroup = fullGroup;
			this.listAttributes = listAttributes;
		}

		@Override
		public void addCompletions(final @NotNull CompletionParameters parameters,
								   final @NotNull ProcessingContext context,
								   final @NotNull CompletionResultSet resultSet)
		{
			final var elem = parameters.getPosition();
			final var group = groupRetriever.apply(elem.getParent());
			computeFromGroup(resultSet, group);
		}

		private void computeFromGroup(final @NotNull CompletionResultSet resultSet, final LMFGroup lmGroup)
		{
			final var astGroup = lmGroup.getNode();
			final var groupNode = PNodeView.of(astGroup);
			final var treeView = new TreeView<>(groupNode, PNodeView::children, PNodeView::parent);

			final var link = PMDEL_BUILDER.linkPartialUnresolved(treeView, (a, b) -> a.printStackTrace());

			if (link != null)
			{
				final var group = link.group();

				if (listAttributes)
				{
					final var computer = new AttributeComputer(listAttributes(group), true);
					final var entries = computer.computeEntries().map(LookupElementBuilder::create).toList();
					resultSet.addAllElements(entries);
				}

				final var computer = new GroupComputer(listRelations(group), fullGroup);
				final var entries = computer.computeEntries().map(LookupElementBuilder::create).toList();
				resultSet.addAllElements(entries);
			}
		}

		public static List<Relation<?, ?>> listRelations(final Group<?> parentGroup)
		{
			return parentGroup.features()
							  .stream()
							  .filter(Relation.class::isInstance)
							  .map(r -> (Relation<?, ?>) r)
							  .collect(Collectors.toUnmodifiableList());
		}

		public static List<Attribute<?, ?>> listAttributes(final Group<?> parentGroup)
		{
			return parentGroup.features()
							  .stream()
							  .filter(Attribute.class::isInstance)
							  .map(r -> (Attribute<?, ?>) r)
							  .collect(Collectors.toUnmodifiableList());
		}
	}
}
