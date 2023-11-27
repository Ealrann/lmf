package org.logoce.lmf.editor.lexer;

import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lexer.LMLexer;
import org.logoce.lmf.model.resource.parsing.LexerException;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.resource.transform.PModelBuilder;

import java.io.IOException;

public final class ModelLexer
{
	private final LMLexer lexer = new LMLexer(null);

	private CharSequence previousBuffer;
	private MetaModel model;

	public void reconciliate(CharSequence text)
	{
		if (!text.equals(previousBuffer))
		{
			previousBuffer = text;
			lexer.reset(text, 0, text.length(), 0);
			model = null;

			final var pmodelBuilder = new org.logoce.lmf.model.resource.parsing.PModelBuilder();
			try
			{
				while (true)
				{
					final var token = lexer.next();
					if (token == null) break;

					final var ptoken = new PToken(lexer.yytext().toString(), token);
					pmodelBuilder.readToken(ptoken);
				}

				model = buildModel(pmodelBuilder);
			}
			catch (LexerException ignored)
			{
			}
			catch (IOException exception)
			{
				exception.printStackTrace();
			}
		}
	}

	public MetaModel getModel()
	{
		return model;
	}

	private MetaModel buildModel(final org.logoce.lmf.model.resource.parsing.PModelBuilder pmodelBuilder)
	{
		final long start = System.currentTimeMillis();
		System.out.println("Start model reconciliation");
		final var trees = pmodelBuilder.buildRoots();
		final var modelBuilder = new PModelBuilder();

		// Fix for intellij ServiceLoader...
		// https://intellij-support.jetbrains.com/hc/en-us/community/posts/206761975-How-to-load-classes-in-a-plugin-with-ServiceLoader-
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

		final var model = (MetaModel) modelBuilder.build(trees).get(0);

		final long duration = System.currentTimeMillis() - start;
		System.out.printf("Model reconciliation done in %1$d ms%n", duration);

		return model;
	}
}
