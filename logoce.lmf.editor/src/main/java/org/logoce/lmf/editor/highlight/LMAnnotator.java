package org.logoce.lmf.editor.highlight;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.psi.LMFGroupType;

public class LMAnnotator implements Annotator
{
	@Override
	public void annotate(@NotNull final PsiElement element, final @NotNull AnnotationHolder holder)
	{

		final var file = element.getContainingFile().getVirtualFile();
		final var project = element.getProject();
		final var lexer = LMSyntaxHighlighterFactory.getLexer(project, file);
		final var model = lexer.getModel();

		if (element instanceof LMFGroupType groupType)
		{
			annotateGroupType(groupType, holder);
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

	private void annotateGroupType(final LMFGroupType groupType, final @NotNull AnnotationHolder holder)
	{
		// Define the text ranges (start is inclusive, end is exclusive)
		// "simple:key"
		//  01234567890
//		final var prefixRange = TextRange.from(groupType.getTextRange().getStartOffset(),
//											   SIMPLE_PREFIX_STR.length() + 1);
//		final var separatorRange = TextRange.from(prefixRange.getEndOffset(), SIMPLE_SEPARATOR_STR.length());
//		final var keyRange = new TextRange(separatorRange.getEndOffset(), groupType.getTextRange().getEndOffset() - 1);

		// highlight "simple" prefix and ":" separator
		holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
			  .range(groupType.getTextRange())
			  .textAttributes(LMSyntaxHighlighter.VALUE)
			  .create();
//		holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
//			  .range(separatorRange)
//			  .textAttributes(LMSyntaxHighlighter.VALUE)
//			  .create();
	}
}
