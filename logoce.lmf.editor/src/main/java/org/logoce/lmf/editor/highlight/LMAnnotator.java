package org.logoce.lmf.editor.highlight;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.parser.LMParserProxy;
import org.logoce.lmf.editor.psi.LMFGroup;
import org.logoce.lmf.editor.psi.LMFVal;

public class LMAnnotator implements Annotator
{
	@Override
	public void annotate(@NotNull final PsiElement element, final @NotNull AnnotationHolder holder)
	{
		/*final var root = element.getContainingFile().getChildren()[0];
		final var textr = root.getText();
		final var astNode = root.getNode();
		final var children = astNode.getChildren(null);
		final var elemType = astNode.getElementType();

		final var containingFile = element.getContainingFile();
		final var file = containingFile.getVirtualFile();
		final var text = containingFile.getText();
		final var project = element.getProject();
		final var lexer = ModelLexerUtil.getOrCreateLexer(project, file);
		lexer.reconciliate(text);
		final var model = lexer.getModel();*/

		/*if (element instanceof LMFVal val)
		{
			final var t = val.getText();
			if (t.startsWith(".") || t.startsWith("/"))
			{
				annotateGroupType(val, holder);
			}
		}
		else */
		if (element instanceof LMFGroup group)
		{
			final var error = group.getNode().getUserData(LMParserProxy.TOKEN_RESOLUTION);
			if (error != null)
			{
				System.out.println("found error");
				holder.newAnnotation(HighlightSeverity.ERROR, error.getMessage())
					  .range(group.getTextRange())
					  .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
					  // ** Tutorial step 19. - Add a quick fix for the string containing possible properties
					  //.withFix(new SimpleCreatePropertyQuickFix(key))
					  .create();
			}
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
