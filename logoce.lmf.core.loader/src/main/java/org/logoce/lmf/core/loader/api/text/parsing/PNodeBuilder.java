package org.logoce.lmf.core.loader.api.text.parsing;

import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;
import org.logoce.lmf.core.util.tree.Tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class PNodeBuilder
{
	private final Deque<PTreeBuilder> stack = new ArrayDeque<>();
	private final Deque<Integer> openOffsets = new ArrayDeque<>();
	private final List<PTreeBuilder> roots = new ArrayList<>();

	public PNodeBuilder()
	{
	}

	public List<Tree<PNode>> buildRoots()
	{
		return roots.stream().map(PTreeBuilder::build).toList();
	}

	public int openDepth()
	{
		return stack.size();
	}

	public Integer lastUnclosedOpenOffset()
	{
		return openOffsets.peekLast();
	}

	public void readToken(final PToken token) throws LexerException
	{
		switch (token.type())
		{
			case OPEN_NODE -> stack(token.offset());
			case CLOSE_NODE -> pop();
			case BAD_CHARACTER -> throw new LexerException("Unexpected character: " + describeBadCharacter(token.value()));
			default -> addToken(token);
		}
	}

	private void stack(final int openOffset)
	{
		final var newNode = stack.isEmpty() ? newRoot() : stack.getLast().newChild();
		stack.add(newNode);
		openOffsets.add(openOffset);
	}

	private PTreeBuilder newRoot()
	{
		final var root = new PTreeBuilder();
		roots.add(root);
		return root;
	}

	private void pop()
		throws LexerException
	{
		if (stack.isEmpty())
		{
			throw new LexerException("Unmatched ')'");
		}
		stack.removeLast();
		if (!openOffsets.isEmpty())
		{
			openOffsets.removeLast();
		}
	}

	private static String describeBadCharacter(final String tokenText)
	{
		if (tokenText == null || tokenText.isEmpty())
		{
			return "<empty>";
		}

		final int codePoint = tokenText.codePointAt(0);
		return switch (codePoint)
		{
			case '\n' -> "'\\n' (U+000A)";
			case '\r' -> "'\\r' (U+000D)";
			case '\t' -> "'\\t' (U+0009)";
			default ->
			{
				final var text = "'" + tokenText + "'";
				yield text + " (U+" + String.format("%04X", codePoint) + ")";
			}
		};
	}

	private void addToken(final PToken token)
	{
		if (!stack.isEmpty()) stack.getLast().addWord(token);
	}
}
