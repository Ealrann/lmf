package org.logoce.lmf.editor.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;
import org.logoce.lmf.model.lexer.ELMTokenType;
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
			return wrapType(type);
		}
	}

	@NotNull
	public static IElementType wrapType(final ELMTokenType type)
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

	@SuppressWarnings("UnstableApiUsage")
	@NotNull
	public static ELMTokenType unwrapType(final IElementType type)
	{
		if(type == LMIntellijTokenTypes.ASSIGN) return ELMTokenType.ASSIGN;
		else if(type == LMIntellijTokenTypes.BAD_CHARACTER) return ELMTokenType.BAD_CHARACTER;
		else if(type == LMIntellijTokenTypes.CLOSE_NODE) return ELMTokenType.CLOSE_NODE;
		else if(type == LMIntellijTokenTypes.LIST_SEPARATOR) return ELMTokenType.LIST_SEPARATOR;
		else if(type == LMIntellijTokenTypes.VALUE_NAME) return ELMTokenType.VALUE_NAME;
		else if(type == LMIntellijTokenTypes.OPEN_NODE) return ELMTokenType.OPEN_NODE;
		else if(type == LMIntellijTokenTypes.QUOTE) return ELMTokenType.QUOTE;
		else if(type == LMIntellijTokenTypes.TYPE) return ELMTokenType.TYPE;
		else if(type == LMIntellijTokenTypes.VALUE) return ELMTokenType.VALUE;
		else if(type == LMIntellijTokenTypes.WHITE_SPACE) return ELMTokenType.WHITE_SPACE;
		else if(type == LMIntellijTokenTypes.TYPE_NAME) return ELMTokenType.TYPE_NAME;
		else throw new IllegalStateException("Invalid type to unwrap : " + type.getDebugName());
	}
}
