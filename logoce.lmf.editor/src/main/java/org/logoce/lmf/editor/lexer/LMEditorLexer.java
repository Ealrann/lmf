package org.logoce.lmf.editor.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.logoce.lmf.editor.psi.LMIntellijTokenTypes;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lexer.ELMTokenType;
import org.logoce.lmf.model.lexer.LMLexer;
import org.logoce.lmf.model.resource.parsing.NodeParser;
import org.logoce.lmf.model.resource.ptree.LexerException;
import org.logoce.lmf.model.resource.ptree.PModelBuilder;
import org.logoce.lmf.model.resource.ptree.PToken;
import org.logoce.lmf.model.resource.transform.PTreeToJava;
import org.logoce.lmf.model.util.ModelRegistry;

public final class LMEditorLexer extends LMLexer implements FlexLexer
{
	private final ModelReconciliator modelReconciliator;

	public LMEditorLexer(boolean resolveModel)
	{
		super(null);
		modelReconciliator = resolveModel ? new ModelReconciliator() : null;
	}

	@Override
	public void reset(final CharSequence buffer, final int start, final int end, final int initialState)
	{
		super.reset(buffer, start, end, initialState);
		if (modelReconciliator != null) modelReconciliator.start(buffer);
	}

	@Override
	public IElementType advance() throws java.io.IOException
	{
		final var type = super.next();

		if (modelReconciliator != null)
		{
			if (type == null) modelReconciliator.stop();
			else modelReconciliator.advance(type, yytext());
		}

		if (type == ELMTokenType.ASSIGN) return LMIntellijTokenTypes.ASSIGN;
		else if (type == ELMTokenType.BAD_CHARACTER) return LMIntellijTokenTypes.BAD_CHARACTER;
		else if (type == ELMTokenType.CLOSE_NODE) return LMIntellijTokenTypes.CLOSE_NODE;
		else if (type == ELMTokenType.LIST_SEPARATOR) return LMIntellijTokenTypes.LIST_SEPARATOR;
		else if (type == ELMTokenType.VALUE_NAME) return LMIntellijTokenTypes.VALUE_NAME;
		else if (type == ELMTokenType.TYPE_NAME) return LMIntellijTokenTypes.TYPE_NAME;
		else if (type == ELMTokenType.OPEN_NODE) return LMIntellijTokenTypes.OPEN_NODE;
		else if (type == ELMTokenType.QUOTE) return LMIntellijTokenTypes.QUOTE;
		else if (type == ELMTokenType.TYPE) return LMIntellijTokenTypes.TYPE;
		else if (type == ELMTokenType.VALUE) return LMIntellijTokenTypes.VALUE;
		else if (type == ELMTokenType.WHITE_SPACE) return LMIntellijTokenTypes.WHITE_SPACE;

		else if (type == null)
		{
			return null;
		}

		throw new IllegalArgumentException();
	}

	public Model getModel()
	{
		assert modelReconciliator != null;
		return modelReconciliator.getModel();
	}

	private static final class ModelReconciliator
	{
		private Model model;
		private CharSequence previousBuffer;
		private boolean resolutionInProgress = false;
		private boolean modelBuilt = false;

		private PModelBuilder pmodelBuilder;

		public ModelReconciliator()
		{}

		public void start(final CharSequence buffer)
		{
			if (buffer.equals(previousBuffer) == false)
			{
				pmodelBuilder = new PModelBuilder(new NodeParser(ModelRegistry.Instance.getAliasMap()));
				resolutionInProgress = true;
				modelBuilt = false;
				previousBuffer = buffer;
			}
		}

		public void stop()
		{
			resolutionInProgress = false;
		}

		public Model getModel()
		{
			if (modelBuilt == false)
			{
				final var trees = pmodelBuilder.buildTrees();
				final var modelBuilder = new PTreeToJava(true);
				model = (Model) modelBuilder.transform(trees).get(0);
			}

			return model;
		}

		public void advance(final ELMTokenType type, final CharSequence yytext)
		{
			if (resolutionInProgress)
			{
				final var token = new PToken(yytext.toString(), type);
				try
				{
					pmodelBuilder.readToken(token);
				}
				catch (LexerException e)
				{
					resolutionInProgress = false;
					model = null;
					modelBuilt = true;

					throw new RuntimeException(e);
				}
			}
		}
	}
}
