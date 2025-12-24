package org.logoce.lmf.lsp.state;

import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;
import org.logoce.lmf.core.util.tree.Tree;

import java.util.List;

public final class SyntaxSnapshot
{
	private final List<PToken> tokens;
	private final List<Tree<PNode>> roots;
	private final List<LmDiagnostic> diagnostics;
	private final CharSequence source;
	private final TextPositions.LineIndex lineIndex;

	public SyntaxSnapshot(final List<PToken> tokens,
						  final List<Tree<PNode>> roots,
						  final List<LmDiagnostic> diagnostics,
						  final CharSequence source)
	{
		this.tokens = List.copyOf(tokens);
		this.roots = List.copyOf(roots);
		this.diagnostics = List.copyOf(diagnostics);
		this.source = source;
		this.lineIndex = TextPositions.index(source);
	}

	public List<PToken> tokens()
	{
		return tokens;
	}

	public List<Tree<PNode>> roots()
	{
		return roots;
	}

	public List<LmDiagnostic> diagnostics()
	{
		return diagnostics;
	}

	public CharSequence source()
	{
		return source;
	}

	public TextPositions.LineIndex lineIndex()
	{
		return lineIndex;
	}
}
