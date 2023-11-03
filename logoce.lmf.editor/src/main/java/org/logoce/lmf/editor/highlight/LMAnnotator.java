package org.logoce.lmf.editor.highlight;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.parser.PNodeView;
import org.logoce.lmf.editor.psi.LMFGroup;
import org.logoce.lmf.editor.psi.LMFVal;
import org.logoce.lmf.model.resource.linking.exception.LinkException;
import org.logoce.lmf.model.resource.transform.PModelBuilder;
import org.logoce.lmf.model.util.tree.TreeView;

public class LMAnnotator implements Annotator
{
	private final static PModelBuilder<PNodeView> PMDEL_BUILDER = new PModelBuilder<>();

	@Override
	public void annotate(@NotNull final PsiElement element, final @NotNull AnnotationHolder holder)
	{
		if (element instanceof LMFGroup group)
		{
			final var astGroup = group.getNode();
			final var groupNode = PNodeView.of(astGroup);
			final var treeView = new TreeView<>(groupNode, PNodeView::children, PNodeView::parent);

			PMDEL_BUILDER.linkPartialUnresolved(treeView, (g, error) -> {
				final var tokens = treeView.data().tokens();
				if (g == groupNode && !tokens.isEmpty())
				{
					final var firstToken = tokens.get(0);
					final var start = group.getTextRange().getStartOffset() + 1;
					final var end = start + firstToken.value().length();
					final var range = new TextRange(start, end);

					holder.newAnnotation(HighlightSeverity.ERROR, error.getMessage())
						  //.range(group.getTextRange())
						  .range(range).highlightType(ProblemHighlightType.ERROR).create();
				}
			});

			/*final var error = group.getNode().getUserData(LMParserProxy.TOKEN_RESOLUTION);
			if (error != null)
			{
				System.out.println("found error");
				holder.newAnnotation(HighlightSeverity.ERROR, error.getMessage())
					  .range(group.getTextRange())
					  .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
					  // ** Tutorial step 19. - Add a quick fix for the string containing possible properties
					  //.withFix(new SimpleCreatePropertyQuickFix(key))
					  .create();
			}*/
		}

		/*// Get the list of properties for given key
		String key = value.substring(SIMPLE_PREFIX_STR.length() + SIMPLE_SEPARATOR_STR.length());
		List<SimpleProperty> properties = SimpleUtil.findProperties(element.getProject(), key);
		if (properties.isEmpty())
		{
			holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved property")
				  .range(keyRange)
				  .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
				  // ** Tutorial step 19. - Add a quick fix for the string containing possible properties
				  .withFix(new SimpleCreatePropertyQuickFix(key))
				  .create();
		}
		else
		{
			// Found at least one property, force the text attributes to Simple syntax value character
			holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
				  .range(keyRange)
				  .textAttributes(SimpleSyntaxHighlighter.VALUE)
				  .create();
		}*/
	}

	private void annotateGroupType(final LMFVal groupType, final @NotNull AnnotationHolder holder)
	{
		holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
			  .range(groupType.getTextRange())
			  .textAttributes(LMSyntaxHighlighter.REFERENCE)
			  .create();
	}
}
