package org.logoce.lmf.editor.format;

import com.intellij.formatting.*;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.LMLanguage;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;

public class LMFormattingModelBuilder implements FormattingModelBuilder
{
	@Override
	public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext)
	{
		final var codeStyleSettings = formattingContext.getCodeStyleSettings();
		return FormattingModelProvider.createFormattingModelForPsiFile(formattingContext.getContainingFile(),
																	   new LMFileBlock(formattingContext.getNode(),
																					   Wrap.createWrap(WrapType.ALWAYS,
																									   true),
																					   Alignment.createAlignment(),
																					   createSpaceBuilder(
																							   codeStyleSettings)),
																	   codeStyleSettings);
	}

	private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings)
	{
		return new SpacingBuilder(settings, LMLanguage.INSTANCE).before(LMIntellijTokenTypes.LEAF)
																.spacing(1, 1, 0, false, 0)
																.before(LMIntellijTokenTypes.GROUP)
																.spacing(0, 0, 1, false, 1);
	}
}
