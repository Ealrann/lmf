package org.logoce.lmf.model.resource.parsing;

import org.logoce.lmf.model.util.tree.Tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class PModelBuilder
{
	private final Deque<PTreeBuilder> stack = new ArrayDeque<>();
	private final List<PTreeBuilder> roots = new ArrayList<>();

	public PModelBuilder()
	{
	}

	public List<Tree<PNode>> buildRoots()
	{
		return roots.stream().map(PTreeBuilder::build).toList();
	}

	public void readToken(final PToken token) throws LexerException
	{
		switch (token.type())
		{
			case OPEN_NODE -> stack();
			case CLOSE_NODE -> pop();
			case BAD_CHARACTER -> throw new LexerException("Invalid model");
			default -> addToken(token);
		}
	}

	private void stack()
	{
		final var newNode = stack.isEmpty() ? newRoot() : stack.getLast().newChild();
		stack.add(newNode);
	}

	private PTreeBuilder newRoot()
	{
		final var root = new PTreeBuilder();
		roots.add(root);
		return root;
	}

	private void pop()
	{
		stack.removeLast();
	}

	private void addToken(final PToken token)
	{
		if (!stack.isEmpty()) stack.getLast().addWord(token);
	}
}
