package org.logoce.lmf.model.loader.parsing;

import org.logoce.lmf.model.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.model.resource.parsing.LMIterableLexer;
import org.logoce.lmf.model.resource.parsing.LexerException;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PNodeBuilder;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.util.TextPositions;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight tree reader that mirrors {@code PTreeReader} but reports diagnostics
 * through {@link LmDiagnostic} and works directly on in-memory {@link CharSequence}.
 */
public final class LmTreeReader
{
	private final LMIterableLexer lexer = new LMIterableLexer();

	public LmTreeReader()
	{
	}

	public ReadResult read(final CharSequence source, final List<LmDiagnostic> diagnostics)
	{
		int lastOffset = 0;
		int lastLength = 1;

		try
		{
			lexer.reset(source, 0);
			final var modelBuilder = new PNodeBuilder();

			for (PToken token : lexer)
			{
				lastOffset = token.offset();
				lastLength = token.length();
				modelBuilder.readToken(token);
			}

			return new ReadResult(modelBuilder.buildRoots(), source);
		}
		catch (LexerException e)
		{
			final int offset = Math.min(source.length(), Math.max(0, lastOffset));
			final int line = TextPositions.lineFor(source, offset);
			final int col = TextPositions.columnFor(source, offset);
			diagnostics.add(new LmDiagnostic(
				line,
				col,
				Math.max(1, lastLength),
				offset,
				LmDiagnostic.Severity.ERROR,
				e.getMessage() == null ? "Parse error" : e.getMessage()
			));
			return new ReadResult(List.of(), source);
		}
	}

	public ReadResult read(final CharSequence source)
	{
		return read(source, new ArrayList<>());
	}

	public record ReadResult(List<Tree<PNode>> roots, CharSequence source)
	{
	}
}
