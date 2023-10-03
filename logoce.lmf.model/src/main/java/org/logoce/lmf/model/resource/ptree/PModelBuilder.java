package org.logoce.lmf.model.resource.ptree;

import org.logoce.lmf.model.resource.parsing.NodeParser;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.Tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class PModelBuilder
{
	private final Deque<PTreeBuilder> stack = new ArrayDeque<>();
	private final List<PTreeBuilder> roots = new ArrayList<>();
	private final NodeParser nodeParser;

	public PModelBuilder(final NodeParser nodeParser)
	{
		this.nodeParser = nodeParser;
	}

	public List<Tree<PNode>> buildTrees()
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
		final var root = new PTreeBuilder(nodeParser);
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
