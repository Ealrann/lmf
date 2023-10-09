package org.logoce.lmf.editor.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;
import org.logoce.lmf.model.lexer.LMLexer;

public final class LMEditorLexer extends LMLexer implements FlexLexer
{
	public LMEditorLexer()
	{
		super(null);
	}

	@Override
	public void reset(final CharSequence buffer, final int start, final int end, final int initialState)
	{
		super.reset(buffer, start, end, initialState);
	}

	@Override
	public IElementType advance() throws java.io.IOException
	{
		final var type = super.next();

		if (type == null)
		{
			return null;
		}
		else
		{
			return switch (type)
			{
				case ASSIGN -> LMIntellijTokenTypes.ASSIGN;
				case BAD_CHARACTER -> LMIntellijTokenTypes.BAD_CHARACTER;
				case CLOSE_NODE -> LMIntellijTokenTypes.CLOSE_NODE;
				case LIST_SEPARATOR -> LMIntellijTokenTypes.LIST_SEPARATOR;
				case VALUE_NAME -> LMIntellijTokenTypes.VALUE_NAME;
				case OPEN_NODE -> LMIntellijTokenTypes.OPEN_NODE;
				case QUOTE -> LMIntellijTokenTypes.QUOTE;
				case TYPE -> LMIntellijTokenTypes.TYPE;
				case VALUE -> LMIntellijTokenTypes.VALUE;
				case WHITE_SPACE -> LMIntellijTokenTypes.WHITE_SPACE;
				case TYPE_NAME -> LMIntellijTokenTypes.TYPE_NAME;
			};
		}
	}
}
